package com.kaleido.fetch.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kaleido.cabinetclient.domain.CabinetPlateMap;
import com.kaleido.fetch.domain.dto.AtlasPlateDTO;
import org.springframework.stereotype.Service;

@Service
public class AtlasPlateMapper {

    public AtlasPlateDTO mapCabinetPlateToAtlasPlateDTO(CabinetPlateMap cabinetPlateMap) throws JsonProcessingException {
        if (cabinetPlateMap == null){
            return null;
        }

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        AtlasPlateDTO atlasPlateDTO = new AtlasPlateDTO();
        atlasPlateDTO = mapper.readValue(cabinetPlateMap.getData(), AtlasPlateDTO.class);
        atlasPlateDTO.setLastModified(cabinetPlateMap.getLastModified());
        return atlasPlateDTO;
    }
}
