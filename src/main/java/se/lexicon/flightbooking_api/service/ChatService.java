package se.lexicon.flightbooking_api.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.dto.QueryDto;

@Service
public class ChatService {

    //Genom chatClient som är mer abstrakt än OpenAiChatModel kan man interagera även med andra företags modeller, såsom
    //Anthropics, HuggingFace, Mistral AI etc.
    private final ChatClient chatClient;
    private ChatMemory chatMemory; //Går att ta bort här
    private final AppToolCalling appToolCalling;

    @Autowired
    public ChatService(ChatClient.Builder chatClient, ChatMemory chatMemory, AppToolCalling appToolCalling/*VectorStore vectorStore*/) {
        this.chatClient = chatClient
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                ).build();
        this.chatMemory = chatMemory; //Går att ta bort här
        this.appToolCalling = appToolCalling;
    }



    public String chatWithMemory(final QueryDto queryDto) {
        if (queryDto.query() == null || queryDto.query().isEmpty() || queryDto.conversationId() == null || queryDto.conversationId().isEmpty()) {
            System.out.println("Query or conversationId is null or empty");
            throw new IllegalArgumentException("query or conversationId can not be null or empty");
        }

       ChatResponse chatResponse = chatClient.prompt()
                .system("""
                        You are a specialized flight management assistant with the following capabilities:
                        1. You can fetch and display all bookings for a given email using the 'checkBooking' tool
                        2. You can delete a booking using the 'cancelBooking' tool
                        3. You can create new bookings using the 'createBooking' tool
                        
                        Guidelines:
                        - Always use the appropriate tool for flight booking management-related operations
                        - Only respond with flight booking-related information
                        - If a request is not about flight bookings, politely explain that you can only help with flight booking management
                        - When displaying flight bookings, present them in a clear, organized manner
                        - Confirm successful operations with brief, clear messages
                        """)
                .user(queryDto.query())
                .tools(appToolCalling)
                .options(OpenAiChatOptions.builder().model("gpt-4.1-mini").temperature(0.3).maxTokens(1000).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, queryDto.conversationId()))
                .call()
                .chatResponse();

        assert chatResponse != null;
        return chatResponse.getResult().getOutput().getText();
    }
}
