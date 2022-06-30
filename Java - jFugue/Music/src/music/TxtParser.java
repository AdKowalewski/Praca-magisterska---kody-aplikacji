package music;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TxtParser {

    public TxtParser() {}

    private String[] noteLetters = new String[] {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    private String[] octaves = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    // Mapa z długościami nut
    private static HashMap<String, String> noteLengthsLetters;

    static {
        noteLengthsLetters = new HashMap<> ();
        noteLengthsLetters.put("1", "w");
        noteLengthsLetters.put("1/2", "h");
        noteLengthsLetters.put("1/4", "q");
        noteLengthsLetters.put("1/8", "i");
        noteLengthsLetters.put("1/16", "s");
        noteLengthsLetters.put("1/32", "t");
    }

    // Metoda przeszukująca powyższą mapę celem dodania odpowiedniej długości
// nuty do tworzonego ciągu instrukcji
    private static String[] addNoteLength(String pattern, String parameter,
                                          String fragment, Boolean isFragment) {
        String newElement = "";
        StringBuilder patternBuilder = new StringBuilder(pattern);
        StringBuilder fragmentBuilder = new StringBuilder(fragment);
        for(Map.Entry<String, String> set : noteLengthsLetters.entrySet()) {
            if(parameter.equals(set.getKey())) {
                newElement = set.getValue();
                patternBuilder.append(newElement);
                if(isFragment) fragmentBuilder.append(newElement);
            }
        }
        fragment = fragmentBuilder.toString();
        pattern = patternBuilder.toString();
        newElement = " ";
        pattern += newElement;
        if(isFragment) fragment += newElement;
        return new String[]{pattern, fragment};
    }

    public String parseTxt(String filePath)
            throws FileNotFoundException {
        String pattern = " "; // pusty ciąg znaków
        File file = new File(filePath); // wczytywanie pliku
        Scanner scanner = new Scanner(file);
        // mapa fragmentów
        Map<String, String> mapOfFragments = new HashMap<>();
        // obiekt dodający nowe elementy do ciągu instrukcji
        String newElement = "";
        // stan określający, czy zapamiętywany jest fragment
        Boolean isFragment = false;
        // zmienna do przechowywania fragmentu
        String fragment = "";
        // zmienna do przechowywania tytułu fragmentu
        String titleOfFragment = "";

        String line; // zmienna do przechowywania linii
        int k = 0; // iterator
        while(scanner.hasNextLine()) { // wczytywanie linii
            k++; // inkrementacja iteratora
            line = scanner.nextLine();
            if(line.isEmpty()) break; // przerwać pętlę,
            // jeśli brak linii
            // ignorować linie zaczynające się na "#" lub "."
            if(line.startsWith("#")) continue;
            if(line.startsWith(".")) {
                System.out.println("End of the song");
                break;
            }
            // podział względem ","
            String[] parameters = line.split("[,]");

            if(parameters.length == 2) {
                // ustawienie tempa
                if (parameters[0].equals("T")) {
                    if (Integer.parseInt(parameters[1]) < 0) {
                        System.out.printf("Tempo in line %d must be greater than 0!\n", k);
                        break;
                    } else {
                        newElement = parameters[0] + parameters[1] + " ";
                        pattern += newElement;
                        if(isFragment) fragment += newElement;
                    }
                }
                // dodanie pauzy
                else if (parameters[0].equals("R")) {
                    newElement = parameters[0];
                    pattern += newElement;
                    if(isFragment) fragment += newElement;
                    pattern = addNoteLength(pattern, parameters[1],
                            fragment, isFragment)[0];
                    fragment = addNoteLength(pattern, parameters[1],
                            fragment, isFragment)[1];
                }
                // rozpoczęcie zapamiętywania fragmentu
                else if (parameters[0].equals("Fragment")) {
                    isFragment = true;
                    titleOfFragment = parameters[1];
                }
                // zakończenie zapamiętywania fragmentu
                else if (parameters[0].equals("End")) {
                    if (!parameters[1].equals(titleOfFragment)) {
                        System.out.printf("Incorrect fragment title in line %d!\n", k);
                        break;
                    } else {
                        mapOfFragments.put(titleOfFragment, fragment);
                        isFragment = false;
                        fragment = "";
                    }
                }
                // wywołanie fragmentu
                else if (parameters[0].equals("Call")) {
                    if (!mapOfFragments.containsKey(parameters[1])) {
                        System.out.printf("Fragment of title %s in line %d does not exist!\n", parameters[1], k);
                        break;
                    } else {
                        pattern += mapOfFragments.get(parameters[1]);
                    }
                } else {
                    System.out.printf("Incorrect first parameter in line %d!\n", k);
                    break;
                }
            } else if(parameters.length == 3) {
                if (!parameters[2].equals("1") && !parameters[2].equals("1/2") && !parameters[2].equals("1/4") &&
                        !parameters[2].equals("1/8") && !parameters[2].equals("1/16") && !parameters[2].equals("1/32")) {
                    System.out.printf("Incorrect note length in line %d!\n", k);
                }
                else if (!Arrays.asList(noteLetters).contains(parameters[0]) || !Arrays.asList(octaves).contains(parameters[1])) {
                    System.out.printf("Note in line %d does not exist!\n", k);
                    break;
                } else {
                    // dodawanie nowej nuty
                    newElement = parameters[0] + parameters[1];
                    pattern += newElement;
                    if(isFragment) fragment += newElement;
                    pattern = addNoteLength(pattern, parameters[2],
                            fragment, isFragment)[0];
                    if (isFragment) {
                        fragment = addNoteLength(pattern, parameters[2],
                                fragment, isFragment)[1];
                    }
                }
            } else {
                System.out.printf("Invalid number of parameters in line %d!\n", k);
                break;
            }
        }
        k = 0;
        return pattern; // zwracanie wzoru
    }
}
