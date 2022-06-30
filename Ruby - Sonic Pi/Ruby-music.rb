#tablica symboli nut
noteLetters = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"]
#tablica numerów oktaw
octaves = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
#słownik długości nut
durationsDict = {
  "1\n" => 4,
  "1/2\n" => 2,
  "1/4\n" => 1,
  "1/8\n" => 0.5,
  "1/16\n" => 0.25,
  "1/32\n" => 0.125
}

#funkcja do tworzenia ścieżki muzycznej na podstawie
#pliku z zapisem muzycznym oraz instrumentu
def parseTxt(filepath, synth)
  #ustawienie instrumentu
  use_synth synth
  #słownik fragmentów
  fragmentDict = {}
  #bigList (znaczenie wyjaśniono w rozdziale 5)
  bigList = []
  #smallList (znaczenie wyjaśniono w rozdziale 5)
  smallList = []
  #stan, czy zapamiętywany jest fragment
  isFragment = 0
  #nazwa fragmentu
  fragmentTitle = ""
  #iterator
  k = 0
  #wczytywanie pliku
  f = File.open(filepath,'r')
  #wczytywanie linii
  f.each_line { |line|
    #inkrementacja iteratora
    k = k + 1
    #ignorowanie linii, jeśli napotkano
    #znak "#" lub "."
    if line[0] == "#"
      next
    end
    if line[0] == "."
      puts "End of the song"
      break
    end
    #podział linii względem ","
    parameters = line.split(",")
    if parameters.length() == 2
      #ustawienie tempa
      if parameters[0] == "T"
        if parameters[1].to_i < 0
          print "Tempo in line #{k} must be greater than 0!"
          break
        else
          use_bpm parameters[1].to_i
        end
      #dodanie pauzy
      elsif parameters[0] == "R"
        durationsDict.each do |key, value|
          if line.end_with?(key)
            sleep value
            if isFragment == 1
              smallList.append("Sleep")
              smallList.append(value)
              bigList.append(smallList)
              smallList = []
            end
          end
        end
      #rozpoczęcie zapamiętywania fragmentu
      elsif parameters[0] == "Fragment"
        isFragment = 1
        fragmentTitle = parameters[1]
      #zakończenie zapamiętywania fragmentu
      elsif parameters[0] == "End"
        isFragment = 0
        if parameters[1] == fragmentTitle
          fragmentDict[fragmentTitle] = bigList
        else
          print "Incorrect fragment title in line #{k}!"
          break
        end
        fragmentTitle = ""
        bigList = []
        smallList = []
      #wywołanie fragmentu
      elsif parameters[0] == "Call"
        if fragmentDict.has_value?(parameters[1])
          fragmentDict[parameters[1]].each do |item|
            if item[0] == "Play"
              play item[1], release: 4
            end
            if item[0] == "Sleep"
              sleep item[1]
            end
          end
        else
          print "Fragment of title #{parameters[1]} in line #{k} does not exist!"
          break
        end
      else
        print "Incorrect first parameter in line #{k}!"
        break
      end
    elsif parameters.length() == 3
      #dodawanie nowej nuty (oraz pauzy potrzebnej
      #na wybrzmienie dźwięku)
      noteLetters.each do |n|
        if parameters[0] == n
          octaves.each do |o|
            if parameters[1] == o
              durationsDict.each do |key, value|
                if line.end_with?(key)
                  play 12*octaves.find_index(o)+noteLetters.find_index(n), release: 4
                  if isFragment == 1
                    smallList.append("Play")
                    smallList.append(12*octaves.find_index(o)+noteLetters.find_index(n))
                    bigList.append(smallList)
                    smallList = []
                  end
                  sleep value
                  if isFragment == 1
                    smallList.append("Sleep")
                    smallList.append(value)
                    bigList.append(smallList)
                    smallList = []
                  end
                end
              end
            end
          end
        end
      end
    else
      print "Invalid number of parameters in line #{k}!"
      break
    end
  }
  k = 0
  f.close
end

in_thread do
  parseTxt 'sciezka_dostepu_do_pliku', :piano}
end
#W przypadku większej ilości plików, do każdego z nich
#należy zastosować odpowiednią ilość bloków "in_thread"