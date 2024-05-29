package ru.inno.tasks;

import lombok.Getter;
import lombok.Setter;

public class Fraction implements Fractionable {

    private int num;
    private int denum;

    @Setter
    @Getter
    private int sum;

    public Fraction(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    @Cache(1000)
    @Override
    public double doubleValue() {
        return (double) num / denum;
    }

    @Mutator
    @Override
    public void setNum(int num) {
        this.num = num;
    }

    @Mutator
    @Override
    public void setDenum(int denum) {
        this.denum = denum;
    }

    @Override
    public double multiplyValue() {
        return (double) num * denum;
    }

    @Cache
    @Override
    public double sumValue() {
        return (double) num + denum;
    }

    @Override
    public void setNumForTest(int num) {
        this.num = num;
    }

}
