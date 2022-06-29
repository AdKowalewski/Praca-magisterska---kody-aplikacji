package music;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GenerateMusic {

    static TxtParser parser = new TxtParser();
    static String instrument = "PIANO";
    static int voice = 0;

    public GenerateMusic() {}

    // Metoda tworząca nową ścieżkę dźwiękową na podstawie podanej
    // ścieżki dostępu do pliku z zapisem muzycznym, numeru ścieżki
    // oraz instrumentu
    public static Pattern melody(String path, int voice,
                                 String instrument) throws FileNotFoundException {
        return new Pattern(parser.parseTxt(path))
                .setVoice(voice)
                .setInstrument(instrument);
    }

    public static void main(String[] args) {
        try {
            Player player = new Player(); // obiekt odtwarzacza
            String instr = ""; // instrument
            int v; // numer ścieżki
            // ustawienie instrumentu
            if (args.length >= 2) {
                instr = args[1];
            } else {
                instr = instrument;
            }
            // ustawienie numeru ścieżki
            if (args.length == 3) {
                v = Integer.parseInt(args[2]);
            } else {
                v = voice;
            }
            // stworzenie nowej ścieżki dźwiękowej
            Pattern pattern = melody(args[0], v, instr);
            // ewentualnie można podać:
            // Pattern pattern =
            //  melody("<sciezka_dostepu_do_pliku>", v, instr);
            // jeśli chcemy podać więcej ścieżek, należy stworzyć
            // kolejne obiekty klasy Pattern,
            // a potem połączyć je w następujący sposób:
            // Pattern inny_pattern =
            //  new Pattern(pattern1, pattern2, ...);

            player.play(pattern); // odtworzenie ścieżki
            // zapis do pliku MIDI
            MidiFileManager.savePatternToMidi(pattern, new File("C:\\Users\\adkow\\Desktop\\koleda.mid"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("Podaj plik z melodią");
            ex.printStackTrace();
        }
    }
}
