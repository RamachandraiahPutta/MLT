package com.example.mlt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Discrepancy {
    private Pair systemClassified;
    private List<Pair> userClassified;
}
