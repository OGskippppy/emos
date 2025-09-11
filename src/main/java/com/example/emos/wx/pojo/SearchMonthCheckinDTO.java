package com.example.emos.wx.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class SearchMonthCheckinDTO {
    private ArrayList<HashMap> list;

    public ArrayList<HashMap> getList() {
        return list;
    }

    public void setList(ArrayList<HashMap> list) {
        this.list = list;
    }

    public int getSum_1() {
        return sum_1;
    }

    public void setSum_1(int sum_1) {
        this.sum_1 = sum_1;
    }

    public int getSum_3() {
        return sum_3;
    }

    public void setSum_3(int sum_3) {
        this.sum_3 = sum_3;
    }

    public int getSum_2() {
        return sum_2;
    }

    public void setSum_2(int sum_2) {
        this.sum_2 = sum_2;
    }

    public SearchMonthCheckinDTO(ArrayList<HashMap> list, int sum_1, int sum_2, int sum_3) {
        this.list = list;
        this.sum_1 = sum_1;
        this.sum_2 = sum_2;
        this.sum_3 = sum_3;
    }

    private int sum_1 ;
    private int sum_2 ;
    private int sum_3 ;
}
