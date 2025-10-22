package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.foundation.Incentive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Component
public class TransactionConsumer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private RestTemplate restTemplate;

    @KafkaListener(topics = "${general.kafka-topic}")
    public void onTransactionReceived(String transactionLine) {
        
        String[] parts = transactionLine.split(",\\s*");
        long senderId = Long.parseLong(parts[0]);
        long recipientId = Long.parseLong(parts[1]);
        float amount = Float.parseFloat(parts[2]);
        Transaction transaction = new Transaction(senderId, recipientId, amount);

        Optional<UserRecord> senderOpt = Optional.ofNullable(userRepository.findById(senderId));
        Optional<UserRecord> recipientOpt = Optional.ofNullable(userRepository.findById(recipientId));

        if (senderOpt.isPresent() && recipientOpt.isPresent()) {
            UserRecord sender = senderOpt.get();
            UserRecord recipient = recipientOpt.get();

            if (sender.getBalance() >= transaction.getAmount()) {
                
                Incentive incentiveObj = restTemplate.postForObject(
                        "http://localhost:8080/incentive",
                        transaction,
                        Incentive.class
                );
                float incentive = incentiveObj != null ? incentiveObj.getAmount() : 0.0f;

                
                sender.setBalance(sender.getBalance() - transaction.getAmount());
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive);

                userRepository.save(sender);
                userRepository.save(recipient);

                
                TransactionRecord record = new TransactionRecord();
                record.setSender(sender);
                record.setRecipient(recipient);
                record.setAmount(transaction.getAmount());
                record.setIncentive(incentive);
                transactionRecordRepository.save(record);

                System.out.println("Processed transaction: " + transaction.getAmount() +
                        ", Incentive: " + incentive);
            } else {
                System.out.println("Invalid transaction: Insufficient funds for sender " + senderId);
            }
        } else {
            System.out.println("Invalid transaction: user(s) not found -- " + senderId + ", " + recipientId);
        }

        
        for (UserRecord user : userRepository.findAll()) {
            if ("wilbur".equalsIgnoreCase(user.getName().trim())) {
                System.out.println("wilbur's current balance: " + user.getBalance());
            }
        }
    }
}
