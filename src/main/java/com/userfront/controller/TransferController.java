package com.userfront.controller;

import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.Recipient;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.User;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.List;

/**
 * Created by maripen on 2016. 12. 26..
 */

@Controller
@RequestMapping("/transfer")
public class TransferController {

    private TransactionService transactionService;
    private UserService userService;

    @Autowired
    public TransferController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @RequestMapping(value = "betweenAccounts", method = RequestMethod.GET)
    public String betweenAccounts(Model model) {
        model.addAttribute("transferFrom", "");
        model.addAttribute("transferTo", "");
        model.addAttribute("amount", "");

        return "betweenAccounts";
    }

    @RequestMapping(value = "betweenAccounts", method = RequestMethod.POST)
    public String betweenAccountsPost(Model model,
                                      @ModelAttribute("transferFrom") String transferFrom,
                                      @ModelAttribute("transferTo") String transferTo,
                                      @ModelAttribute("amount") String amount,
                                      Principal principal
    ) throws Exception {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        transactionService.betweenAccountsTransfer(transferFrom, transferTo, amount, primaryAccount, savingsAccount);

        return "redirect:/userFront";
    }

    @RequestMapping(value = "/recipient", method = RequestMethod.GET)
    public String recipient(Model model, Principal principal) {
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        Recipient recipient = new Recipient();

        model.addAttribute("recipientList" , recipientList);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping(value = "/recipient/save",method = RequestMethod.POST)
    public String recipientPost(@ModelAttribute Recipient recipient,
                                Principal principal) {
        User user = userService.findByUsername(principal.getName());

        recipient.setUser(user);
        transactionService.saveRecipient(recipient);

        return "redirect:/transfer/recipient";
    }

    @RequestMapping(value = "/recipient/edit",method = RequestMethod.GET)
    public String recipientPost(@RequestParam(value = "recipientName") String recipientName,
                                Model model,
                                Principal principal) {

        Recipient recipient = transactionService.findRecipientByName(recipientName);
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        model.addAttribute("recipient", recipient);
        model.addAttribute("recipientList", recipientList);

        transactionService.saveRecipient(recipient);

        return "/recipient";
    }

    @RequestMapping(value = "/recipient/delete",method = RequestMethod.GET)
    @Transactional
    public String recipientDelete(@RequestParam(value = "recipientName") String recipientName,
                                Model model,
                                Principal principal) {

        transactionService.deleteRecipientByName(recipientName);
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        Recipient recipient = new Recipient();

        model.addAttribute("recipient", recipient);
        model.addAttribute("recipientList", recipientList);

        return "recipient";
    }
}
