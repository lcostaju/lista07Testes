package com.iftm.client.resources;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.ClientService;

//necessário para utilizar o MockMVC
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTest {
    @Autowired
    private MockMvc mockMVC;

    @MockBean
    private ClientService service;

    /**
     * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os clientes existentes
     * Arrange:
     * - camada service simulada com mockito
     * - base de dado : 3 clientes
     * new Client(7l, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(8l, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0);
     * - Uma PageRequest default
     * @throws Exception 
     */
    @Test
    @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
    public void testarEndPointListarTodosClientesRetornaCorreto() throws Exception{
        //arrange
        int quantidadeClientes = 3;
        //configurando o Mock ClientService
        List<ClientDTO> listaClientes;
        listaClientes = new ArrayList<ClientDTO>();
        listaClientes.add(new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientes.add(new ClientDTO(new Client(8L, "Toni Morrison", "10219344681", 10000.0,Instant.parse("1940-02-23T07:00:00Z"), 0)));

        Page<ClientDTO> page = new PageImpl<>(listaClientes);

        Mockito.when(service.findAllPaged(Mockito.any())).thenReturn(page);        
        //fim configuração mockito

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
            .andExpect(jsonPath("$.totalElements").value(quantidadeClientes));


    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/id/{id} retorna o cliente correto quando o id existe (mock)")
    public void testarEndPointBuscarPorIdExistenteMockado() throws Exception {
        Long existingId = 7L;
        ClientDTO clientDTO = new ClientDTO(new Client(7L, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0));
        Mockito.when(service.findById(existingId)).thenReturn(clientDTO);
        ResultActions result = mockMVC.perform(get("/clients/id/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.name").value("Jose Saramago"));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/id/{id} retorna erro 404 quando o id não existe (mock)")
    public void testarEndPointBuscarPorIdInexistenteMockado() throws Exception {
        Long nonExistingId = 999L;
        Mockito.when(service.findById(nonExistingId)).thenThrow(new com.iftm.client.services.exceptions.ResourceNotFoundException("Entity not found"));
        ResultActions result = mockMVC.perform(get("/clients/id/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/income/ retorna clientes com income igual ao informado (mock)")
    public void testarEndPointFindByIncomeRetornaClientesMockado() throws Exception {
        double salario = 1500.0;
        List<ClientDTO> lista = List.of(
            new ClientDTO(new Client(1L, "Cliente 1", "111", salario, Instant.now(), 0)),
            new ClientDTO(new Client(2L, "Cliente 2", "222", salario, Instant.now(), 1))
        );
        Page<ClientDTO> page = new PageImpl<>(lista);
        Mockito.when(service.findByIncome(Mockito.any(), Mockito.eq(salario))).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/income/").param("income", String.valueOf(salario)).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/income/ retorna lista vazia quando não há clientes (mock)")
    public void testarEndPointFindByIncomeRetornaListaVaziaMockado() throws Exception {
        double salario = 9999.0;
        Page<ClientDTO> page = new PageImpl<>(List.of());
        Mockito.when(service.findByIncome(Mockito.any(), Mockito.eq(salario))).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/income/").param("income", String.valueOf(salario)).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/incomeGreaterThan/ retorna clientes com income maior que o informado (mock)")
    public void testarEndPointFindByIncomeGreaterThanRetornaClientesMockado() throws Exception {
        double salario = 1000.0;
        List<ClientDTO> lista = List.of(
            new ClientDTO(new Client(1L, "Cliente 1", "111", 2000.0, Instant.now(), 0)),
            new ClientDTO(new Client(2L, "Cliente 2", "222", 3000.0, Instant.now(), 1))
        );
        Page<ClientDTO> page = new PageImpl<>(lista);
        Mockito.when(service.findByIncomeGreaterThan(Mockito.any(), Mockito.eq(salario))).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/incomeGreaterThan/").param("income", String.valueOf(salario)).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/incomeGreaterThan/ retorna lista vazia quando não há clientes (mock)")
    public void testarEndPointFindByIncomeGreaterThanRetornaListaVaziaMockado() throws Exception {
        double salario = 9999.0;
        Page<ClientDTO> page = new PageImpl<>(List.of());
        Mockito.when(service.findByIncomeGreaterThan(Mockito.any(), Mockito.eq(salario))).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/incomeGreaterThan/").param("income", String.valueOf(salario)).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/cpf/ retorna clientes cujo CPF contém o valor informado (mock)")
    public void testarEndPointFindByCPFLikeRetornaClientesMockado() throws Exception {
        String cpfParcial = "111";
        List<ClientDTO> lista = List.of(
            new ClientDTO(new Client(1L, "Cliente 1", cpfParcial, 2000.0, Instant.now(), 0))
        );
        Page<ClientDTO> page = new PageImpl<>(lista);
        Mockito.when(service.findByCpfLike(Mockito.any(), Mockito.anyString())).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/cpf/").param("cpf", cpfParcial).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Verificar se o endpoint get/clients/cpf/ retorna lista vazia quando não há clientes (mock)")
    public void testarEndPointFindByCPFLikeRetornaListaVaziaMockado() throws Exception {
        String cpfParcial = "000";
        Page<ClientDTO> page = new PageImpl<>(List.of());
        Mockito.when(service.findByCpfLike(Mockito.any(), Mockito.anyString())).thenReturn(page);
        ResultActions result = mockMVC.perform(get("/clients/cpf/").param("cpf", cpfParcial).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Verificar se o endpoint POST /clients/ cria um novo cliente e retorna status 201 e os dados corretos (mock)")
    public void testarEndPointInsertRetornaCreatedEClienteCriadoMockado() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Novo Cliente Teste");
        clientDTO.setCpf("12345678901");
        clientDTO.setIncome(1234.56);
        clientDTO.setChildren(1);
        clientDTO.setBirthDate(Instant.parse("2000-01-01T00:00:00Z"));
        Mockito.when(service.insert(Mockito.any())).thenReturn(clientDTO);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(clientDTO);
        ResultActions result = mockMVC.perform(post("/clients/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Novo Cliente Teste"))
            .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    @DisplayName("Verificar se o endpoint PUT /clients/{id} atualiza e retorna o cliente correto quando o id existe (mock)")
    public void testarEndPointUpdateRetornaOkEClienteAtualizadoMockado() throws Exception {
        Long idExistente = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Nome Atualizado");
        clientDTO.setCpf("99999999999");
        clientDTO.setIncome(2000.0);
        clientDTO.setChildren(2);
        clientDTO.setBirthDate(Instant.parse("2000-01-01T00:00:00Z"));
        Mockito.when(service.update(Mockito.eq(idExistente), Mockito.any())).thenReturn(clientDTO);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(clientDTO);
        ResultActions result = mockMVC.perform(put("/clients/{id}", idExistente)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nome Atualizado"))
            .andExpect(jsonPath("$.cpf").value("99999999999"));
    }

    @Test
    @DisplayName("Verificar se o endpoint PUT /clients/{id} retorna 404 quando o id não existe (mock)")
    public void testarEndPointUpdateRetornaNotFoundQuandoIdNaoExisteMockado() throws Exception {
        Long idInexistente = 999L;
        Mockito.when(service.update(Mockito.eq(idInexistente), Mockito.any())).thenThrow(new com.iftm.client.services.exceptions.ResourceNotFoundException("Entity not found"));
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Qualquer Nome");
        clientDTO.setCpf("00000000000");
        clientDTO.setIncome(100.0);
        clientDTO.setChildren(0);
        clientDTO.setBirthDate(Instant.parse("2000-01-01T00:00:00Z"));
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(clientDTO);
        ResultActions result = mockMVC.perform(put("/clients/{id}", idInexistente)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verificar se o endpoint DELETE /clients/{id} retorna 204 quando o id existe (mock)")
    public void testarEndPointDeleteRetornaNoContentQuandoIdExisteMockado() throws Exception {
        Long idExistente = 1L;
        Mockito.doNothing().when(service).delete(idExistente);
        mockMVC.perform(delete("/clients/{id}", idExistente))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Verificar se o endpoint DELETE /clients/{id} retorna 404 quando o id não existe (mock)")
    public void testarEndPointDeleteRetornaNotFoundQuandoIdNaoExisteMockado() throws Exception {
        Long idInexistente = 999L;
        Mockito.doThrow(new com.iftm.client.services.exceptions.ResourceNotFoundException("Entity not found")).when(service).delete(idInexistente);
        mockMVC.perform(delete("/clients/{id}", idInexistente))
            .andExpect(status().isNotFound());
    }
}
