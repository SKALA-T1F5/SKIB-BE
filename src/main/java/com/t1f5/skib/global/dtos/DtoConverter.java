package com.t1f5.skib.global.dtos;

@FunctionalInterface
public interface DtoConverter<E, D> {
  D convert(E entity);
}
