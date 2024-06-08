package br.ufpr.tads.catalog.catalog.domain.service;


import br.ufpr.tads.catalog.catalog.domain.response.ReceiptResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @RabbitListener(queues = "${broker.queue.receipt-scan.name}")
    public void listen(String message) {
        try {
            ReceiptResponseDTO response = objectMapper.reader().forType(ReceiptResponseDTO.class).readValue(message);
            log.info("Received message: "+response);
            productService.processReceipt(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
