package ru.advancedapiary;
public final class ApiaryLayout {
    public static final int QUEENS=0, FLOWERS=6, MODULES=10, OUTPUT=18, SIZE=33;
    public static final int WIDTH=248, HEIGHT=238;
    public static int queenX(int i) { return 13+(i%3)*56; }
    public static int queenY(int i) { return 39+(i/3)*48; }
    private ApiaryLayout() {}
}
