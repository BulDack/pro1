package com.example.demo.ennum;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;

import java.util.function.BiFunction;

public enum SearchOperator {

    EQ((path,val)->path.eq(val)), //EQUAL
    NE((path,val)->path.ne(val)), //not equal
    GOE((path,val)->path.goe(val)),
    LOE((path,val)->path.loe(val));

    //NumberExpression<Integer> (1번 입력): Querydsl에서 숫자를 나타내는 컬럼 객체입니다. (예: 아까 새로 만드신 숫자형 컬럼인 subAttr.numberValue)
    //Integer (2번 입력): 사용자가 검색창에 입력한 진짜 숫자 값입니다. (예: 20, 100 등)
    //BooleanExpression (최종 결과): 1번 입력과 2번 입력을 조합해서 만든 Querydsl 조건절(WHERE 문에 들어갈 쪼가리)입니다. (예: subAttr.numberValue.goe(20))
    private final BiFunction<NumberExpression<Integer>,Integer, BooleanExpression>expression;

    SearchOperator(BiFunction<NumberExpression<Integer>, Integer, BooleanExpression> expression){
        this.expression=expression;
    }

    public BooleanExpression apply(NumberExpression<Integer>path,Integer value){
        return this.expression.apply(path,value);
    }



}
