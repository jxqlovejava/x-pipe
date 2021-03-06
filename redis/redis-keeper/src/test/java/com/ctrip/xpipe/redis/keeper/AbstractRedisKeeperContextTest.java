package com.ctrip.xpipe.redis.keeper;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.xml.sax.SAXException;

import com.ctrip.xpipe.api.cluster.LeaderElectorManager;
import com.ctrip.xpipe.redis.core.entity.ClusterMeta;
import com.ctrip.xpipe.redis.core.entity.DcMeta;
import com.ctrip.xpipe.redis.core.entity.KeeperMeta;
import com.ctrip.xpipe.redis.core.entity.RedisMeta;
import com.ctrip.xpipe.redis.core.entity.ShardMeta;
import com.ctrip.xpipe.redis.core.entity.XpipeMeta;
import com.ctrip.xpipe.redis.core.metaserver.MetaServerKeeperService;
import com.ctrip.xpipe.redis.keeper.config.KeeperConfig;
import com.ctrip.xpipe.redis.keeper.impl.DefaultRedisKeeperServer;
import com.ctrip.xpipe.redis.keeper.spring.KeeperContextConfig;

/**
 * @author wenchao.meng
 *
 *         Jun 12, 2016
 */
public class AbstractRedisKeeperContextTest extends AbstractRedisKeeperTest {

	protected MetaServerKeeperService  metaService;
	
	protected KeeperConfig  keeperConfig;
	
	private String keeperConfigFile = "keeper6666.xml";

	private int keeperServerPortMin = 7777, keeperServerPortMax = 7877;

	@Before
	public void beforeAbstractRedisKeeperTest() throws Exception {
		
		doIdcInit();
		
		metaService = getRegistry().getComponent(MetaServerKeeperService.class);
		keeperConfig = getRegistry().getComponent(KeeperConfig.class);
		
	}
	
	@Override
	protected ConfigurableApplicationContext createSpringContext() {
		return new AnnotationConfigApplicationContext(KeeperContextConfig.class);
	}

	protected void doIdcInit() {
	}

	protected KeeperMeta createKeeperMeta() throws SAXException, IOException {

		return createKeeperMeta(randomPort(keeperServerPortMin, keeperServerPortMax));
	}


	protected KeeperMeta createKeeperMeta(int port) throws SAXException, IOException {

		XpipeMeta xpipe = loadXpipeMeta(getXpipeMetaConfigFile());
		for(DcMeta dcMeta : xpipe.getDcs().values()){
			for(ClusterMeta clusterMeta : dcMeta.getClusters().values()){
				for(ShardMeta shardMeta : clusterMeta.getShards().values()){
					for(KeeperMeta keeperMeta : shardMeta.getKeepers()){
						keeperMeta.setPort(port);
						keeperMeta.setActive(true);
						keeperMeta.setId(randomString(40));
						return keeperMeta;
					}
				}
			}
		}
		return null;
	}

	protected String getKeeperConfigFile() {
		return keeperConfigFile;
	}

	protected RedisKeeperServer createRedisKeeperServer(KeeperConfig keeperConfig) throws Exception {
		
		return createRedisKeeperServer(createKeeperMeta(), keeperConfig, metaService, getReplicationStoreManagerBaseDir());
	}
	
	protected RedisKeeperServer createRedisKeeperServer() throws Exception {

		return createRedisKeeperServer(createKeeperMeta());
	}

	protected RedisKeeperServer createRedisKeeperServer(KeeperMeta keeperMeta) throws Exception {

		return createRedisKeeperServer(keeperMeta, metaService);
	}
	
	protected RedisKeeperServer createRedisKeeperServer(KeeperMeta keeper, MetaServerKeeperService metaService) throws Exception {
		return createRedisKeeperServer(keeper, metaService, getReplicationStoreManagerBaseDir());
	}

	protected RedisKeeperServer createRedisKeeperServer(KeeperMeta keeper, MetaServerKeeperService metaService, File baseDir) throws Exception {

		return createRedisKeeperServer(keeper, getKeeperConfig(), metaService, baseDir);

	}

	protected KeeperConfig getKeeperConfig() {
		return keeperConfig;
	}

	protected RedisKeeperServer createRedisKeeperServer(KeeperMeta keeper, KeeperConfig keeperConfig, MetaServerKeeperService metaService, File baseDir) throws Exception {

		return createRedisKeeperServer(keeper, keeperConfig, metaService, baseDir, getRegistry().getComponent(LeaderElectorManager.class));
	}

	protected RedisKeeperServer createRedisKeeperServer(KeeperMeta keeper, KeeperConfig keeperConfig,
			MetaServerKeeperService metaService, File baseDir, LeaderElectorManager leaderElectorManager) {
		return new DefaultRedisKeeperServer(keeper, keeperConfig, baseDir, metaService, leaderElectorManager, createkeepersMonitorManager());
	}

	protected RedisMeta createRedisMeta() {
		
		return createRedisMeta("localhost", randomPort());
	}

	protected RedisMeta createRedisMeta(String host, int port) {
		
		RedisMeta redisMeta = new RedisMeta();
		redisMeta.setIp(host);
		redisMeta.setPort(port);
		return redisMeta;
	}

	@Override
	protected String getXpipeMetaConfigFile() {
		return null;
	}
}
