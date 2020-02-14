package org.flowable.rest.events;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


public class CustomEventListener implements FlowableEventListener {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CustomEventListener.class);
	
	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

	private static final String BPM_ACTION = "/bpm-action";
    
	private RestTemplate restTemplate;
	
	private String defaulDjAdapterUrl;
    
	
	
	public CustomEventListener(String defaulDjAdapterUrl) {
		super();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        factory.setConnectTimeout(2000);
        restTemplate = new RestTemplate(factory);
        this.defaulDjAdapterUrl = defaulDjAdapterUrl;
	}

	
	@Override
	public void onEvent(FlowableEvent event) {
		THREAD_POOL.execute(() -> {
   		 try {
   				FlowableEventType eventType = event.getType();
   				
   				if (eventType == FlowableEngineEventType.TASK_COMPLETED) {

   				    if(LOGGER.isInfoEnabled()) {
   				    	String message = "EventListener raised .....of Type: " + eventType + " Class: " + event.getClass();
   				    	LOGGER.info(message);
   				    }
   				    			    
   				    Map<String, Object> body = new HashMap<>();
   				    body.put("taskId", ((TaskEntity)((FlowableEntityEventImpl) event).getEntity()).getId());

   				    if(LOGGER.isInfoEnabled()) {
   				    	LOGGER.info("Calling with: " + body);
   				    }
   				    
   				    restTemplate.put(defaulDjAdapterUrl + BPM_ACTION, body);
   				    
   				    if(LOGGER.isInfoEnabled()) {
   				    	LOGGER.info("Called Rest without exceptions!!");
   				    }
   				}
   			} catch (Exception e) {
   				LOGGER.error(e.getMessage(), e);
   			}
   		
   	});
	}



	@Override
	public boolean isFireOnTransactionLifecycleEvent() {
		return false;
	}



	@Override
	public String getOnTransaction() {
		return null;
	}

	@Override
	public boolean isFailOnException() {
		LOGGER.error("FailOnException!!");
		return false;
	}


}
