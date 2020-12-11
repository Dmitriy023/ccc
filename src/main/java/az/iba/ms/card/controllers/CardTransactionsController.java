package az.iba.ms.card.controllers;

import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.dtos.ufx.TransactionDto;
import az.iba.ms.card.enums.ReturnTypes;
import az.iba.ms.card.services.CardTransactionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(CardTransactionsController.ENDPOINT)
@Api(produces = MediaType.APPLICATION_JSON_VALUE, tags = "Card Transactions")
public class CardTransactionsController {

    public static final String ENDPOINT = "/v1/cards/{contractNumber}/transactions";

    @Autowired
    private CardTransactionsService cardTransactionsService;

    @ApiOperation("Returns card transactions of contractNumber")
    @GetMapping
    public ResponseEntity<ResponseDto<List<TransactionDto>>> getCardTransactions(
            @ApiParam(
                    name = "contractNumber",
                    type = "String",
                    value = "Contract Number",
                    example = "5490349934171340",
                    required = true)
            @PathVariable("contractNumber")
            @Pattern(regexp = "\\d+")
                    String contractNumber,
            @ApiParam(
                    name = "from",
                    type = "String",
                    value = "Begin date filter",
                    example = "2020-09-20",
                    required = true)
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
                    String from,
            @ApiParam(
                    name = "to",
                    type = "String",
                    value = "End date filter",
                    example = "2020-09-21",
                    required = true)
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
                    String to,
            @ApiParam(
                    name = "pagesize",
                    type = "String",
                    value = "List size of a page",
                    example = "1231",
                    allowableValues = "range[1, 100]",
                    required = true)
            @RequestParam
            @Min(1)
            @Max(100)
                    int pagesize,
            @ApiParam(
                    name = "page",
                    type = "String",
                    value = "Current page index",
                    example = "1",
                    allowableValues = "range[1, 100]",
                    required = true)
            @RequestParam
            @Min(1)
            @Max(100)
                    int page) {

        List<TransactionDto> transactionDtos =
                cardTransactionsService.getCardTransactions(
                        contractNumber, from, to, pagesize, page);

        ResponseDto<List<TransactionDto>> response =
                new ResponseDto<>(transactionDtos, ReturnTypes.OK.toString());

        return ResponseEntity.ok(response);
    }
}
