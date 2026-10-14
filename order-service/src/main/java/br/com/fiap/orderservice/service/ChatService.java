package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.exception.AssistantUnavailableException;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    private static final String SYSTEM_MESSAGE = """
        Você é o atendente virtual do restaurante Diamante Delivery.
        Regras:
        - Responda sempre em português do Brasil, com educação, em no máximo 3 frases curtas.
        - Use SOMENTE o cardápio enviado na mensagem do usuário. Nunca invente pratos, ingredientes ou preços.
        - Os preços estão em reais (R$). Um prato com estoque 0 está esgotado: informe isso ao cliente.
        - Você pode ajudar com pratos, ingredientes, preços, disponibilidade, sugestões e pedidos.
        - Se a pergunta não for sobre o restaurante, recuse com educação em uma frase e
          ofereça ajuda com o cardápio. Não responda a pergunta fora do tema.
        """;

    private final ChatClient chatClient;
    private final DishRepository dishRepository;

    public ChatService(ChatClient.Builder chatClientBuilder, DishRepository dishRepository) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_MESSAGE).build();
        this.dishRepository = dishRepository;
    }

    public String ask(String question) {
        String menu = formatMenu(dishRepository.findAllByOrderByIdAsc());
        String userMessage = """
                Current menu (name | price | stock | description):
                %s

                Customer question: %s
                """.formatted(menu, question);

        try {
            String answer = chatClient.prompt().user(userMessage).call().content();
            if (answer == null || answer.isBlank()) {
                throw new IllegalStateException("Empty answer from the model");
            }
            return answer.strip();
        } catch (RuntimeException e) {
            log.error("AI model call failed: {}", e.getMessage());
            throw new AssistantUnavailableException();
        }
    }

    private String formatMenu(List<Dish> dishes) {
        return dishes.stream()
                .map(dish -> "- %s | R$ %s | stock %d | %s".formatted(
                        dish.getName(), dish.getPrice(), dish.getStock(), dish.getDescription()))
                .collect(Collectors.joining("\n"));
    }

}
