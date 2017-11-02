package cn.zhijian.shipper;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.db.CorporateMapper;

public class MultiThreadTest extends IntegrationTestSupport{

	private static Logger logger = LoggerFactory.getLogger(MultiThreadTest.class);
	
	@Test
	public void dbMultiThread() throws Exception {
		CorporateMapper mapper = this.getMapper(CorporateMapper.class);
	    for (int i = 0; i < 10; i++) {
	    	Thread t = new Thread(()->{
	    		try{
	    			mapper.findCorporateApplication(1L);
	    			System.out.println("线程执行完毕");
	    		}
	    		catch (Exception e) {
	    			System.err.println("异常:"+e.getMessage());
	    		}
	    	});
	    	t.start();
	    }
	}
}