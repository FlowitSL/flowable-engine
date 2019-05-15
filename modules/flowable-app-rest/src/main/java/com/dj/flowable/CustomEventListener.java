package com.dj.flowable;

import org.flowable.engine.common.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEventListener;
import org.flowable.engine.common.api.delegate.event.FlowableEventType;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomEventListener implements FlowableEventListener {

	private static Logger logger = LoggerFactory.getLogger(CustomEventListener.class);
	
	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(20);

	private static final String BPM_ACTION = "/bpm-action";

	private static final int READ_TIMEOUT       = 10000;
    private static final int CONNECTION_TIMEOUT = 10000;

    private String baseEntryPoint;
    
    public CustomEventListener(String url) {
    	super();
   		baseEntryPoint = url;
    	
    }

	public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // Be careful without this all Tomcat can freeze by not responding request !!!
        factory.setReadTimeout(READ_TIMEOUT);
        factory.setConnectTimeout(CONNECTION_TIMEOUT);

        return factory;
    }


    @Override
    public void onEvent(FlowableEvent event) {

    	
    	THREAD_POOL.execute(() -> {
    		 try {
    				FlowableEventType eventType = event.getType();
    				
    				if (eventType == FlowableEngineEventType.TASK_COMPLETED) {

    				    if(logger.isInfoEnabled()) {
    				    	String message = "EventListener raised .....of Type: " + eventType + " Class: " + event.getClass();
    				    	logger.info(message);
    				    }
    				    			    
    				    Map<String, Object> body = new HashMap<>();
    				    body.put("taskId", ((TaskEntity)((FlowableEntityEventImpl) event).getEntity()).getId());

    				    if(logger.isInfoEnabled()) {
    				    	logger.info("Calling with: " + body);
    				    }
    				    
    				    restTemplate().put(baseEntryPoint + BPM_ACTION, body);
    				    
    				    if(logger.isInfoEnabled()) {
    				    	logger.info("Called Rest without exceptions!!");
    				    }
    				}
    			} catch (Exception e) {
    				logger.error(e.getMessage(), e);
    			}
    		
    	});

    }


	@Override
    public boolean isFailOnException() {
        logger.error( "FailOnException !!");
        return false;
    }


}
