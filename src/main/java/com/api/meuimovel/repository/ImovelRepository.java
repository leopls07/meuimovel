package com.api.meuimovel.repository;

import com.api.meuimovel.model.Imovel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ImovelRepository extends MongoRepository<Imovel, String> {

    /** Busca todos os imóveis de um usuário */
    List<Imovel> findAllByUserId(String userId);

    /** Busca um imóvel garantindo que pertence ao usuário — evita acesso cruzado */
    Optional<Imovel> findByIdAndUserId(String id, String userId);
}

