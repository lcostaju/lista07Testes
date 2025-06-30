package com.iftm.client.resources;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.iftm.client.services.ClientService;

//necessário para utilizar o MockMVC
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceIntegrationTest {
    @Autowired
    private MockMvc mockMVC;

    @Autowired
    private ClientService service;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os clientes existentes
     * Arrange:
     * - base de dado : 12 clientes
     * - Uma PageRequest default
     * @throws Exception 
     */
    @Test
    @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
    public void testarEndPointListarTodosClientesRetornaCorreto() throws Exception{
        //arrange
        int quantidadeClientes = 12;
        int quantidadeLinhasPagina = 12;

        //act

        ResultActions resultados = mockMVC.perform(get("/clients/").accept(MediaType.APPLICATION_JSON));

        //assign
        resultados
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[?(@.id == '%s')]",7L).exists())
            .andExpect(jsonPath("$.content[?(@.id == '%s')]",4L).exists())
            .andExpect(jsonPath("$.content[?(@.id == '%s')]",8L).exists())
            .andExpect(jsonPath("$.content[?(@.name == '%s')]","Toni Morrison").exists())
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.totalElements").value(quantidadeClientes))
            .andExpect(jsonPath("$.numberOfElements").exists())
            .andExpect(jsonPath("$.numberOfElements").value(quantidadeLinhasPagina))
            .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4,10,3,1,6,5,12,7,2,11,8,9)))
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.pageable.pageSize").value(12))
            .andExpect(jsonPath("$.content[?(@.id == 3)].name").value("Clarice Lispector"))
            .andExpect(jsonPath("$.content[?(@.id == 5)].income").value(2500.0))
            .andExpect(jsonPath("$.content[?(@.id == 1)].children").value(2));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/id/{id} retorna o cliente correto quando o id existe")
    public void testarEndPointBuscarPorIdExistenteRetornaClienteCorreto() throws Exception {
        Long existingId = 3L;
        ResultActions result = mockMVC.perform(get("/clients/id/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.name").value("Clarice Lispector"))
            .andExpect(jsonPath("$.cpf").value("10919444522"))
            .andExpect(jsonPath("$.income").value(3800.0))
            .andExpect(jsonPath("$.birthDate").value("1960-04-13T07:50:00Z"))
            .andExpect(jsonPath("$.children").value(2));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/id/{id} retorna erro 404 e mensagem adequada quando o id não existe")
    public void testarEndPointBuscarPorIdInexistenteRetornaErro404() throws Exception {
        Long nonExistingId = 33L;
        ResultActions result = mockMVC.perform(get("/clients/id/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource not found"))
            .andExpect(jsonPath("$.message").value("Entity not found"))
            .andExpect(jsonPath("$.path").value("/clients/id/33"));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/income/ retorna clientes com income igual ao informado (base import.sql)")
    public void testarEndPointFindByIncomeRetornaClientesCorretos() throws Exception {
        double salarioResultado = 1500.0;
        int quantidadeEsperada = 3; // Conceição Evaristo, Yuval Noah Harari, Chimamanda Adichie
        ResultActions result = mockMVC.perform(get("/clients/income/")
            .param("income", String.valueOf(salarioResultado))
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(quantidadeEsperada))
            .andExpect(jsonPath("$.content[*].income", everyItem(is(salarioResultado))))
            .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(
                "Conceição Evaristo",
                "Yuval Noah Harari",
                "Chimamanda Adichie"
            )));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/income/ retorna lista vazia quando não há clientes com o income informado")
    public void testarEndPointFindByIncomeRetornaListaVazia() throws Exception {
        double salarioInexistente = 999999.0;
        ResultActions result = mockMVC.perform(get("/clients/income/")
            .param("income", String.valueOf(salarioInexistente))
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/incomeGreaterThan/ retorna clientes com income maior que o informado (base import.sql)")
    public void testarEndPointFindByIncomeGreaterThanRetornaClientesCorretos() throws Exception {
        double salarioMinimo = 5000.0;
        int quantidadeEsperada = 3; // Carolina Maria de Jesus (7500), Toni Morrison (10000), Jose Saramago (5000)
        ResultActions result = mockMVC.perform(get("/clients/incomeGreaterThan/")
            .param("income", String.valueOf(salarioMinimo))
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(
                "Carolina Maria de Jesus",
                "Toni Morrison"
            )))
            .andExpect(jsonPath("$.content[*].income", everyItem(greaterThan(salarioMinimo))));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/incomeGreaterThan/ retorna lista vazia quando não há clientes com income maior que o informado")
    public void testarEndPointFindByIncomeGreaterThanRetornaListaVazia() throws Exception {
        double salarioMuitoAlto = 20000.0;
        ResultActions result = mockMVC.perform(get("/clients/incomeGreaterThan/")
            .param("income", String.valueOf(salarioMuitoAlto))
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/cpfLike/ retorna clientes cujo CPF contém o valor informado (base import.sql)")
    public void testarEndPointFindByCPFLikeRetornaClientesCorretos() throws Exception {
        String cpfParcial = "10619244881"; // Presente em Conceição Evaristo, Lázaro Ramos, Yuval Noah Harari
        int quantidadeEsperada = 3;
        ResultActions result = mockMVC.perform(get("/clients/cpf/")
            .param("cpf", cpfParcial)
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(quantidadeEsperada))
            .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(
                "Conceição Evaristo",
                "Lázaro Ramos",
                "Yuval Noah Harari"
            )));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/cpfLike/ retorna lista vazia quando não há clientes com o CPF informado")
    public void testarEndPointFindByCPFLikeRetornaListaVazia() throws Exception {
        String cpfInexistente = "00000000000";
        ResultActions result = mockMVC.perform(get("/clients/cpf/")
            .param("cpf", cpfInexistente)
            .accept(MediaType.APPLICATION_JSON));
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint POST /clients/ cria um novo cliente e retorna status 201 e os dados corretos")
    public void testarEndPointInsertRetornaCreatedEClienteCriado() throws Exception {
        // Arrange
        com.iftm.client.dto.ClientDTO clientDTO = new com.iftm.client.dto.ClientDTO();
        clientDTO.setName("Novo Cliente Teste");
        clientDTO.setCpf("12345678901");
        clientDTO.setIncome(1234.56);
        clientDTO.setChildren(1);
        clientDTO.setBirthDate(java.time.Instant.parse("2000-01-01T00:00:00Z"));
        String json = objectMapper.writeValueAsString(clientDTO);

        // Act
        ResultActions result = mockMVC.perform(post("/clients/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        // Assert
        result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Novo Cliente Teste"))
            .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    @DisplayName("Verificar se o endpoint DELETE /clients/{id} retorna 204 quando o id existe")
    public void testarEndPointDeleteRetornaNoContentQuandoIdExiste() throws Exception {
        Long idExistente = 3L; // Clarice Lispector
        mockMVC.perform(delete("/clients/{id}", idExistente))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Verificar se o endpoint DELETE /clients/{id} retorna 404 quando o id não existe")
    public void testarEndPointDeleteRetornaNotFoundQuandoIdNaoExiste() throws Exception {
        Long idInexistente = 999L;
        mockMVC.perform(delete("/clients/{id}", idInexistente))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verificar se o endpoint PUT /clients/{id} atualiza e retorna o cliente correto quando o id existe")
    public void testarEndPointUpdateRetornaOkEClienteAtualizado() throws Exception {
        // Arrange
        Long idExistente = 3L; // Clarice Lispector
        String nomeAtualizado = "Clarice Lispector Atualizada";
        String cpfAtualizado = "99999999999";
        com.iftm.client.dto.ClientDTO clientDTO = new com.iftm.client.dto.ClientDTO();
        clientDTO.setName(nomeAtualizado);
        clientDTO.setCpf(cpfAtualizado);
        clientDTO.setIncome(3800.0);
        clientDTO.setChildren(2);
        clientDTO.setBirthDate(java.time.Instant.parse("1960-04-13T07:50:00Z"));
        String json = objectMapper.writeValueAsString(clientDTO);

        // Act
        ResultActions result = mockMVC.perform(put("/clients/{id}", idExistente)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        // Assert
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(nomeAtualizado))
            .andExpect(jsonPath("$.cpf").value(cpfAtualizado));
    }

    @Test
    @DisplayName("Verificar se o endpoint PUT /clients/{id} retorna 404 e mensagem adequada quando o id não existe")
    public void testarEndPointUpdateRetornaNotFoundQuandoIdNaoExiste() throws Exception {
        // Arrange
        Long idInexistente = 999L;
        com.iftm.client.dto.ClientDTO clientDTO = new com.iftm.client.dto.ClientDTO();
        clientDTO.setName("Qualquer Nome");
        clientDTO.setCpf("00000000000");
        clientDTO.setIncome(100.0);
        clientDTO.setChildren(0);
        clientDTO.setBirthDate(java.time.Instant.parse("2000-01-01T00:00:00Z"));
        String json = objectMapper.writeValueAsString(clientDTO);

        // Act
        ResultActions result = mockMVC.perform(put("/clients/{id}", idInexistente)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        // Assert
        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Resource not found"));
    }
}
