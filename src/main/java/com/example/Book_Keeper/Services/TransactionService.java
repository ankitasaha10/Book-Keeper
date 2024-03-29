package com.example.Book_Keeper.Services;


import com.example.Book_Keeper.DTOs.IssueBookRequestDTO;
import com.example.Book_Keeper.Enum.CardStatus;
import com.example.Book_Keeper.Enum.TransactionStatus;
import com.example.Book_Keeper.Models.Book;
import com.example.Book_Keeper.Models.Card;
import com.example.Book_Keeper.Models.Transaction;
import com.example.Book_Keeper.Repositories.BookRepository;
import com.example.Book_Keeper.Repositories.CardRepository;
import com.example.Book_Keeper.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {


    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    BookRepository bookRepository;

    @Autowired
    CardRepository cardRepository;



    public  String issueBook(IssueBookRequestDTO issueBookRequestDto)throws Exception{


        int bookId = issueBookRequestDto.getBookId();
        int cardId = issueBookRequestDto.getCardId();

        //Get the Book Entity and Card Entity ??? Why do we need this
        //We are this bcz we want to set the Transaction attributes...

        Book book = bookRepository.findById(bookId).get();

        Card card = cardRepository.findById(cardId).get();


        //Final goal is to make a transaction Entity, set its attribute
        //and save it.
        Transaction transaction = new Transaction();

        //Setting the attributes
        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.PENDING);


        //attribute left is success/Failure
        //Check for validations
        if(book==null || book.isIssued()==true){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new Exception("Book is not available");

        }

        if(card==null || (card.getCardStatus()!= CardStatus.ACTIVATED)){

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw  new Exception("Card is not valid");
        }



        //We have reached a success case now.

        transaction.setTransactionStatus(TransactionStatus.SUCCESS);


        //set attributes of book
        book.setIssued(true);
        //Btw the book and transaction : bidirectional
        List<Transaction> listOfTransactionForBook = book.getListOfTransactions();
        listOfTransactionForBook.add(transaction);
        book.setListOfTransactions(listOfTransactionForBook);


        //I need to make changes in the card
        //Book and the card
        List<Book> issuedBooksForCard = card.getBooksIssued();
        issuedBooksForCard.add(book);
        card.setBooksIssued(issuedBooksForCard);

//        for(Book b: issuedBooksForCard){
//            System.out.println(b.getName());
//        }

        //Card and the Transaction : bidirectional (parent class)
        List<Transaction> transactionsListForCard = card.getTransactionList();
        transactionsListForCard.add(transaction);
        card.setTransactionList(transactionsListForCard);

        //save the parent.
        cardRepository.save(card);
        //automatically by cascading : book and transaction will be saved.
        //Saving the parent

        return "Book issued successfully";
    }

    public String getTransactions(int bookId, int cardId){
        List<Transaction> transactionList= transactionRepository.getTransactionsForBookAndCard(bookId,cardId);
        String transactionId= transactionList.get(0).getTransactionId();
        return transactionId;
    }
}
