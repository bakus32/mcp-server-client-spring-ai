package ci.bakus.younous.mcpclient.controllers;

import ci.bakus.younous.mcpclient.agents.MyAIAgent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin("*")
public class ChatController {
    private final MyAIAgent agent;

    public ChatController(MyAIAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/chat/send")
    @ResponseBody
    public String sendMessage(@RequestParam String message) {
        return agent.prompt(message);
    }
}