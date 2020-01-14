package guru.springframework.sfgjms.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgjms.config.JmsConfig;
import guru.springframework.sfgjms.model.HelloWorldMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    //@Scheduled(fixedRate = 2000)
    public void sendMessage() {
        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello World")
                .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);
    }


    @Scheduled(fixedRate = 2000)
    public void sendAndReceiveMessage() throws JMSException {
        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message received = jmsTemplate.sendAndReceive(JmsConfig.SEND_RECEIVE_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message1 = null;
                try {
                    message1 = session.createTextMessage(objectMapper.writeValueAsString(message));
                    message1.setStringProperty("_type", "guru.springframework.sfgjms.model.HelloWorldMessage");
                    System.out.println("Sending...");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return message1;
            }
        });

        System.out.println("Received: " + received.getBody(String.class));
    }
}