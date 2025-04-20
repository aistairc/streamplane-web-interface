package jp.go.aist.streamplane.streamplaneweb;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class StreamPlaneController {

    @RequestMapping(value = "/getJobData", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getJobData(@RequestParam(value = "jobId", defaultValue = "") String jobId) {
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses("localhost");
        IgniteClient igniteClient = Ignition.startClient(cfg);
        for(String cacheName : igniteClient.cacheNames()) {
            if(cacheName.startsWith(jobId + "-task-")) {
                ClientCache<String, String> taskCache = igniteClient.cache(cacheName);
                Integer parallelism = Integer.valueOf(taskCache.get("parallelism"));
                for(Integer i = 0; i < parallelism; i++) {
                    String taskName = taskCache.get("task-name-" + i);
                    String allocationId = taskCache.get("allocation-id-" + i);
                    String instanceStatus = taskCache.get("instance-status-" + i);
                    String inputStream = taskCache.get("input-stream-" + i);
                    String outputStream = taskCache.get("output-stream-" + i);

                    System.out.println(taskName + " " + allocationId + " " + instanceStatus + " " + inputStream + " " + outputStream);
                }
            }
        }
        return Collections.singletonMap("response", "Hello World");
    }

    @RequestMapping(value = "/setInstanceStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map setInstanceStatus(@RequestParam(value = "operatorCacheKey", defaultValue = "") String operatorCacheKey,
                         @RequestParam(value = "subtaskIndex", defaultValue = "0") String subtaskIndex,
                         @RequestParam(value = "status", defaultValue = "Running") String status) {
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses("localhost");
        IgniteClient igniteClient = Ignition.startClient(cfg);
        igniteClient.cache(operatorCacheKey).put("instance-status-" + subtaskIndex, status);
        return Collections.singletonMap("response", "success");
    }

    @RequestMapping(value = "/convertChannel", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map convertChannel(@RequestParam(value = "outputStreamId", defaultValue = "") String outputStreamId,
                         @RequestParam(value = "channelIndex", defaultValue = "0") String channelIndex,
                         @RequestParam(value = "mode", defaultValue = "in-memory") String mode) {
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses("localhost");
        IgniteClient igniteClient = Ignition.startClient(cfg);
        if(mode.equals("in-memory")) {
            igniteClient.cache(outputStreamId).put(channelIndex, outputStreamId + "-" + channelIndex);
        } else {
            igniteClient.cache(outputStreamId).remove(channelIndex);
        }
        return Collections.singletonMap("response", "success");
    }

    @RequestMapping(value = "/setInputStream", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map setInputStream(@RequestParam(value = "operatorCacheKey", defaultValue = "") String operatorCacheKey,
                                 @RequestParam(value = "subtaskIndex", defaultValue = "0") String subtaskIndex,
                                 @RequestParam(value = "inputStreamId", defaultValue = "Running") String inputStreamId) {
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses("localhost");
        IgniteClient igniteClient = Ignition.startClient(cfg);
        igniteClient.cache(operatorCacheKey).put("input-stream-" + subtaskIndex, inputStreamId);
        return Collections.singletonMap("response", "success");
    }

    @RequestMapping(value = "/setOutputStream", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map setOutputStream(@RequestParam(value = "operatorCacheKey", defaultValue = "") String operatorCacheKey,
                              @RequestParam(value = "subtaskIndex", defaultValue = "0") String subtaskIndex,
                              @RequestParam(value = "outputStreamId", defaultValue = "Running") String outputStreamId) {
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setAddresses("localhost");
        IgniteClient igniteClient = Ignition.startClient(cfg);
        igniteClient.cache(operatorCacheKey).put("output-stream-" + subtaskIndex, outputStreamId);
        return Collections.singletonMap("response", "success");
    }

}
