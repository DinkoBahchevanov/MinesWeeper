package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    private int probes;
    private int disposal;
    private int minesCount;
    private int currentRow;
    private int currentCol;
    private boolean isDead = false;
    private boolean win = false;

    /**
     * В този метод се извършва цялата "игра". В него "зареждаме" полето
     * (също и допълнителен двумерен масивмасив, в който да пазим координатите на мините)
     * и ни се предоставя шанса да избираме от 3 команди:
     * 1. Проба за мина
     * 2. Обезвреждане на мина
     * 3. Преместване на бойната станция
     */

    public void play() {
        showRules();

        Scanner scanner = new Scanner(System.in);
        String[][] field = loadField();
        for (String[] strings : field) {
            Arrays.fill(strings, "X");
        }

        loadEntryAndExit(field);
        readAndSetProbesAndDisposal();

        String[][] arrayToHoldMines = new String[field.length][field[0].length];
        for (String[] strings : arrayToHoldMines) {
            Arrays.fill(strings, "");
        }
        loadMines(this.minesCount, field, arrayToHoldMines);

        printField(field);

        while (!isDead || field[currentCol][currentRow].equals("F")) {
            printMenu();
            int chosenCommand = scanner.nextInt();
            switch (chosenCommand) {
                case 1:
                    if (this.probes < 0) {
                        System.out.println("Нямаш повече проби!");
                        continue;
                    }
                    probeForMine(field, arrayToHoldMines, scanner);
                    break;
                case 2:
                    defuseMine(field, arrayToHoldMines, scanner);
                    break;
                case 3:
                    move(field, arrayToHoldMines, scanner);
                    if (isDead) {
                        System.out.println("Попадна на мина! Край на играта!");
                        return;
                    }
                    if (win) return;
                    break;
            }
            printField(field);
        }
    }

    private void showRules() {
        try {
            File fileReference = new File("loadingField/Rules");
            FileReader fileReferenceReader = new FileReader(fileReference);
            BufferedReader bufferedReader = new BufferedReader(fileReferenceReader);

            String line = bufferedReader.readLine();
            while (line != null) {
                System.out.println(line);
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Чрез този метод обезвреждаме мината ако:
     *  - имаме право на обезвреждане
     *  - има мина на посочените координати
     *  Ако няма мина, прекратяваме действието на метода
     * @param field - това е главното поле(двумерен масив), което се принтира на конзолата.
     * @param arrayToHoldMines - това е второстепенното поле(двумерен масив),
     * в което пазим координатите на мините
     */
    private void defuseMine(String[][] field, String[][] arrayToHoldMines, Scanner scanner) {

        System.out.print("Въведи ред: ");
        int row = scanner.nextInt();
        System.out.println();
        System.out.print("Въведи колона: ");
        int col = scanner.nextInt();
        System.out.println();

        while ((row >= field.length || row < 0 || col >= field[0].length || col < 0)
                || (this.currentRow - row > 1 || this.currentRow - row < -1)
                || (this.currentCol - col > 1 || this.currentCol - col < -1)) {

            System.out.println("Въведи отново правилни координати: ");
            System.out.print("Въведи ред: ");
            row = scanner.nextInt();
            System.out.println();
            System.out.print("Въведи колона: ");
            col = scanner.nextInt();
            System.out.println();
        }
        if (arrayToHoldMines[row][col].equals("M")) {
            if (disposal < 0) {
                System.out.println("Нямаш право на обезвреждания!");
                return;
            }
            arrayToHoldMines[row][col] = "";
            this.disposal--;
            field[row][col] = "N";
            System.out.println("Успешно обезвредена мина!");
        } else {
            System.out.printf("На координати %d:%d няма мина!%n", row, col);
            field[row][col] = "N";
        }
    }

    /**
     * В този метод извършваме придвижването на бойната станция.
     * Ако на посочените координати за придвижване има мина, която е разкрита,
     * тя бива обезвредена и се осъществява преминаване. Ако има мина, но не е
     * разкрита, играчът губи. Ако няма мина се осъществява преминаване.
     * @param field - това е главното поле(двумерен масив), което се принтира на конзолата.
     * @param arrayToHoldMines - това е второстепенното поле(двумерен масив),
     * в което пазим координатите на мините
     */
    private void move(String[][] field, String[][] arrayToHoldMines, Scanner scanner) {

        System.out.print("Въведи ред: ");
        int row = scanner.nextInt();
        System.out.println();
        System.out.print("Въведи колона: ");
        int col = scanner.nextInt();
        System.out.println();

        while ((row >= field.length || row < 0 || col >= field[0].length || col < 0)
                || (this.currentRow - row > 1 || this.currentRow - row < -1)
                || (this.currentCol - col > 1 || this.currentCol - col < -1)) {

            System.out.println("Въведи отново правилни координати: ");
            System.out.print("Въведи ред: ");
            row = scanner.nextInt();
            System.out.println();
            System.out.print("Въведи колона: ");
            col = scanner.nextInt();
            System.out.println();
        }
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j].equals("*")) field[i][j] = "V";
            }
        }

        int rowOfExit = 0;
        int colOfExit = 0;

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j].equals("F")) {
                    rowOfExit = i;
                    colOfExit = j;
                }
            }
        }
        if (arrayToHoldMines[row][col].equals("M") && field[row][col].equals("Y") && this.disposal > 0) {
            currentRow = row;
            currentCol = col;
            System.out.println("Преместване на бойната станция и обезвреждане на мината!");
            field[currentRow][currentCol] = "*";
            this.disposal--;

        } else if ((arrayToHoldMines[row][col].equals("M") || field[row][col].equals("Y"))) {
            this.isDead = true;
        } else {
            currentRow = row;
            currentCol = col;
            System.out.println("Преместване на бойната станция!");
            field[currentRow][currentCol] = "*";
        }
        if (currentRow == rowOfExit && currentCol == colOfExit) {
            win = true;
            System.out.println("Ти победи! Край на играта!");
        }
    }

    /**
     * В този метод проверяваме дали на посочените координати има мина или не.
     * @param field - това е главното поле(двумерен масив), което се принтира на конзолата.
     * @param arrayToHoldMines - това е второстепенното поле(двумерен масив),
     * в което пазим координатите на мините
     */
    private void probeForMine(String[][] field, String[][] arrayToHoldMines, Scanner scanner) {

        System.out.print("Въведи ред: ");
        int row = scanner.nextInt();
        System.out.println();
        System.out.print("Въведи колона: ");
        int col = scanner.nextInt();
        System.out.println();

        while ((row >= field.length || row < 0 || col >= field[0].length || col < 0)
                || (this.currentRow - row > 1 || this.currentRow - row < -1)
                || (this.currentCol - col > 1 || this.currentCol - col < -1)) {
            System.out.println("Въведи отново правилни координати: ");
            System.out.print("Въведи ред: ");
            row = scanner.nextInt();
            System.out.println();
            System.out.print("Въведи колона: ");
            col = scanner.nextInt();
            System.out.println();

        }

        if (!arrayToHoldMines[row][col].equals("M")) {
            field[row][col] = "N";
            System.out.printf("На позиция %d:%d няма мина!%n", row, col);
        } else {
            System.out.printf("На позиция %d:%d има мина!%n", row, col);
            field[row][col] = "Y";
        }
        this.probes--;
    }

    private void printField(String[][] field) {
        System.out.print("  ");
        for (int i = 0; i <field[0].length; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i = 0; i < field.length; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < field[i].length; j++) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println();

        }
    }

    private void loadMines(int minesCount, String[][] field, String[][] arrayToHoldMines) {
        Random random = new Random();
        while (minesCount > 0) {
            int row = random.nextInt(field.length);
            int col = random.nextInt(field[0].length);

            if (!arrayToHoldMines[row][col].equals("M")
                    && !field[row][col].equals("F")
                    && !field[row][col].equals("S")) {

                arrayToHoldMines[row][col] = "M";
                minesCount--;
            }
        }
    }

    private void printMenu() {
        System.out.println("1. Проба за мина\n" +
                "2. Обезвреждане на мина\n" +
                "3. (пре)Мини");
    }

    //reading and setting disposal and probes
    private void readAndSetProbesAndDisposal() {
        Pattern pattern = Pattern.compile("[a-z]+_[a-z]+_[a-z]+=(\\d+)");
        try {
            File fileReference = new File("loadingField/configurations.txt");
            FileReader fileReferenceReader = new FileReader(fileReference);
            BufferedReader bufferedReader = new BufferedReader(fileReferenceReader);

            for (int i = 0; i < 2; i++) {
                String line = bufferedReader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (i == 0) {
                        this.probes = Integer.parseInt(matcher.group(1));
                    } else {
                        this.disposal = Integer.parseInt(matcher.group(1));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of probes: " + this.probes);
        System.out.println("Number of disposal: " + this.disposal);
    }

    //loading randomly the entry "S" and exit "F" of field
    private void loadEntryAndExit(String[][] field) {
        Random random = new Random();

        int exit = random.nextInt(field.length);
        while (exit != 0 && exit != field[field[0].length].length - 1) {
            exit = random.nextInt(field.length - 1);
        }
        field[0][exit] = "F";

        int entry = random.nextInt();
        if (field[0][0].equals("F")) {
            field[field.length - 1][field[field.length - 1].length - 1] = "S";
            this.currentRow = field.length - 1;
            this.currentCol = field[field.length - 1].length - 1;
        } else {
            field[field.length - 1][0] = "S";
            this.currentRow = field.length - 1;
            this.currentCol = 0;
        }
        System.out.println();
    }

    //reading from file and loading the field with its dimensions
    private String[][] loadField() {
        int rows = 0;
        int cols = 0;
        try {
            File fileReference = new File("loadingField/enemy_teritory.txt");
            FileReader fileReferenceReader = new FileReader(fileReference);
            BufferedReader bufferedReader = new BufferedReader(fileReferenceReader);

            for (int i = 0; i < 3; i++) {
                String line = bufferedReader.readLine();
                if (i == 0) {
                    for (int j = 0; j < line.length(); j++) {
                        if (Character.isDigit(line.charAt(j))) {
                            rows = Integer.parseInt(String.valueOf(line.charAt(j)));
                            break;
                        }
                    }
                } else if (i == 1) {
                    for (int j = 0; j < line.length(); j++) {
                        if (Character.isDigit(line.charAt(j))) {
                            cols = Integer.parseInt(String.valueOf(line.charAt(j)));
                            break;
                        }
                    }
                } else {
                    for (int j = 0; j < line.length(); j++) {
                        if (Character.isDigit(line.charAt(j))) {
                            this.minesCount = Integer.parseInt(String.valueOf(line.charAt(j)));
                            break;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[rows][cols];
    }
}
