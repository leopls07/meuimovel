package com.api.meuimovel.repository;

import com.api.meuimovel.model.Imovel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImovelRepository extends MongoRepository<Imovel, String> {
}

