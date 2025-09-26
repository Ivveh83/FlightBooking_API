package se.lexicon.flightbooking_api.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import se.lexicon.flightbooking_api.dto.QueryDto;

@Service
public class ChatService {

    final String SYSTEMPROMPT = """
            You are a specialized flight management assistant with the following capabilities:
            1. **Check bookings** using the `checkBooking` tool (requires: **email**)
            2. **Cancel a booking** using the `cancelBooking` tool (requires: **email**, **flightId**)
            3. **Create a new booking** using the `createBooking` tool (requires: **email**, **name**, **flightId**)
            
            Guidelines:
            - Always use the appropriate tool with the required parameters.
            - Only handle requests related to flight bookings. If a request is unrelated, politely explain that you can only assist with flight booking management.
            - Present booking details in a clear and organized way when showing results.
            - Confirm successful operations with concise and unambiguous messages.
            """;

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


    public Flux<String> chatWithMemoryAndTools(final QueryDto queryDto) {
        if (queryDto.query() == null || queryDto.query().isEmpty() || queryDto.conversationId() == null || queryDto.conversationId().isEmpty()) {
            System.out.println("Query or conversationId is null or empty");
            throw new IllegalArgumentException("query or conversationId can not be null or empty");
        }
        try {
            return chatClient.prompt()
                    .system(SYSTEMPROMPT)
                    .user(queryDto.query())
                    .tools(appToolCalling)
                    .options(OpenAiChatOptions.builder().model("gpt-4.1-mini").temperature(0.3).maxTokens(1000).build())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, queryDto.conversationId()))
                    .stream()
                    .chatResponse()
                    .mapNotNull(cr -> cr.getResult().getOutput().getText());
        } catch (Exception e) {
            return Flux.just("Sorry, something went wrong");
        }

    }
}
