package com.hiring.service;

import com.hiring.dto.SolutionRequest;
import com.hiring.dto.WebhookRequest;
import com.hiring.dto.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class WebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    
    private static final String GENERATE_WEBHOOK_URL = 
        "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    
    private static final String SQL_QUERY = 
        "WITH HighEarners AS (" +
        "    SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT, " +
        "           d.DEPARTMENT_NAME, p.AMOUNT " +
        "    FROM EMPLOYEE e " +
        "    INNER JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
        "    INNER JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
        "    WHERE p.AMOUNT > 70000" +
        "), " +
        "RankedEmployees AS (" +
        "    SELECT DEPARTMENT_NAME, EMP_ID, FIRST_NAME, LAST_NAME, DOB, DEPARTMENT, " +
        "           ROW_NUMBER() OVER (PARTITION BY DEPARTMENT ORDER BY EMP_ID) as rn " +
        "    FROM HighEarners " +
        "    GROUP BY DEPARTMENT_NAME, EMP_ID, FIRST_NAME, LAST_NAME, DOB, DEPARTMENT" +
        ") " +
        "SELECT r.DEPARTMENT_NAME, " +
        "       ROUND(AVG(TIMESTAMPDIFF(YEAR, r.DOB, CURDATE())), 2) as AVERAGE_AGE, " +
        "       GROUP_CONCAT(CONCAT(r.FIRST_NAME, ' ', r.LAST_NAME) " +
        "                    ORDER BY r.EMP_ID SEPARATOR ', ') as EMPLOYEE_LIST " +
        "FROM RankedEmployees r " +
        "WHERE r.rn <= 10 " +
        "GROUP BY r.DEPARTMENT_NAME, r.DEPARTMENT " +
        "ORDER BY r.DEPARTMENT DESC";
    
    private final RestTemplate restTemplate;
    
    public WebhookService() {
        this.restTemplate = new RestTemplate();
    }
    
    public void executeWebhookFlow() {
        try {
            logger.info("Starting webhook flow...");
            
            WebhookResponse webhookResponse = generateWebhook();
            
            if (webhookResponse == null || webhookResponse.getWebhook() == null) {
                logger.error("Failed to generate webhook");
                return;
            }
            
            logger.info("Webhook generated successfully");
            logger.info("Webhook URL: {}", webhookResponse.getWebhook());
            
            submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken());
            
            logger.info("Webhook flow completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in webhook flow: {}", e.getMessage(), e);
        }
    }
    
    private WebhookResponse generateWebhook() {
        try {
            WebhookRequest request = new WebhookRequest(
                "Parth Suri",
                "22BDS0116",
                "parthsuri009@gmail.com"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                GENERATE_WEBHOOK_URL,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error generating webhook: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private void submitSolution(String webhookUrl, String accessToken) {
        try {
            SolutionRequest request = new SolutionRequest(SQL_QUERY);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);
            
            HttpEntity<SolutionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            logger.info("Solution submitted successfully");
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response body: {}", response.getBody());
            
        } catch (Exception e) {
            logger.error("Error submitting solution: {}", e.getMessage(), e);
        }
    }
}