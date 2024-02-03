package dev.charizard.messagebroker.services;

import dev.charizard.messagebroker.dtos.ReceivedTransactionDTO;
import dev.charizard.messagebroker.models.Installment;
import dev.charizard.messagebroker.models.Person;
import dev.charizard.messagebroker.models.Transaction;
import dev.charizard.messagebroker.repositories.InstallmentRepository;
import dev.charizard.messagebroker.repositories.PersonRepository;
import dev.charizard.messagebroker.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TransactionService {
	@Autowired
	TransactionRepository transactionRepository;
	@Autowired
	InstallmentRepository installmentRepository;
	@Autowired
	PersonRepository personRepository;

	@Transactional
	public void processTransaction(ReceivedTransactionDTO comingTransaction) {
		var quantityInstallments = comingTransaction.getInstallmentQuantity();
		Optional<Person> personFromDb = personRepository.findById(comingTransaction.getDocument());
		if (personFromDb.isEmpty()) {
			var person = Person.create(
							comingTransaction.getDocument(),
							comingTransaction.getName(),
							comingTransaction.getAge()
			);
			// create one installment for each monthv
			var transaction = Transaction.create(
							comingTransaction.getTransactionId(),
							person,
							comingTransaction.getTransactionDate(),
							comingTransaction.getValue()
			);
			var installments = new HashSet<Installment>();
			for (int i = 1; i <= quantityInstallments; i++) { // popula a tabela de installment com as parcelas
				var installment = Installment.create(
								i,
								comingTransaction.getValue() / quantityInstallments
				);
				installment.setTransaction(transaction);
				installments.add(installment);
			}
			transaction.setInstallments(installments);
			transaction.setPerson(person);
			person.setTransactions(Set.of(transaction));
			transactionRepository.save(transaction);
		}


	}
}
