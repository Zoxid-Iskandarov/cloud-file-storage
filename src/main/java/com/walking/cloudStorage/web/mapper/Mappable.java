package com.walking.cloudStorage.web.mapper;

public interface Mappable<E, D> {
    D toDto(E entity);

    E toEntity(D dto);
}
