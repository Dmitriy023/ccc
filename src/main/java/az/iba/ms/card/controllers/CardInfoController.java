package az.iba.ms.card.controllers;

import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.enums.ReturnTypes;
import az.iba.ms.card.services.CardInfoService;
import az.iba.ms.card.validators.CifListParamConstraint;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(CardInfoController.ENDPOINT)
@Api(produces = MediaType.APPLICATION_JSON_VALUE, tags = "Card Info")
public class CardInfoController {

    public static final String ENDPOINT = "/v1/cards";

    @Autowired
    private CardInfoService cardInfoService;

    @ApiOperation("Returns card info of given cif list")
    @GetMapping
    public ResponseEntity<ResponseDto<List<CardInfoDto>>> getCardInfo(
            @ApiParam(
                    name = "cif-list",
                    type = "String",
                    value = "Comma separated customer ids, max size defined in yml",
                    example = "1839347 or 1839347,23121",
                    required = true)
            @RequestParam(name = "cif-list")
            @Pattern(regexp = "\\d{7}(,\\d{7})*")
            @CifListParamConstraint
                    String cifs) {

        ResponseDto<List<CardInfoDto>> response =
                new ResponseDto<>(cardInfoService.getCardInfo(cifs), ReturnTypes.OK.toString());

        return ResponseEntity.ok(response);
    }
}
