#tablica symboli nut
noteLetters = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"]
#tablica numerów oktaw
octaves = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
#s³ownik d³ugoœci nut
durationsDict = {
  "1\n" => 4,
  "1/2\n" => 2,
  "1/4\n" => 1,
  "1/8\n" => 0.5,
  "1/16\n" => 0.25,
  "1/32\n" => 0.125
}

#funkcja do tworzenia œcie¿ki muzycznej na podstawie
#pliku z zapisem muzycznym oraz instrumentu
def parseTxt(filepath, synth)
  #ustawienie instrumentu
  use_synth synth
  #s³ownik fragmentów
  fragmentDict = {}
  #bigList (znaczenie wyjaœniono w rozdziale 5)
  bigList = []
  #smallList (znaczenie wyjaœniono w rozdziale 5)
  smallList = []
  #stan, czy zapamiêtywany jest fragment
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
    #ignorowanie linii, jeœli napotkano
    #znak "#" lub "."
    if line[0] == "#"
      next
    end
    if line[0] == "."
      next
    end
    #podzia³ linii wzglêdem ","
    parameters = line.split(",")
    if parameters.length() == 2
      #ustawienie tempa
      if parameters[0] == "T"
        use_bpm parameters[1].to_i
      end
      #dodanie pauzy
      if parameters[0] == "R"
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
      end
      #rozpoczêcie zapamiêtywania fragmentu
      if parameters[0] == "Fragment"
        isFragment = 1
        fragmentTitle = parameters[1]
      end
      #zakoñczenie zapamiêtywania fragmentu
      if parameters[0] == "End"
        isFragment = 0
        if parameters[1] == fragmentTitle
          fragmentDict[fragmentTitle] = bigList
        end
        fragmentTitle = ""
        bigList = []
        smallList = []
      end
      #wywo³anie fragmentu
      if parameters[0] == "Call"
        fragmentDict[parameters[1]].each do |item|
          if item[0] == "Play"
            play item[1], release: 4
          end
          if item[0] == "Sleep"
            sleep item[1]
          end
        end
      end
    elsif parameters.length() == 3
      #dodawanie nowej nuty (oraz pauzy potrzebnej
      #na wybrzmienie dŸwiêku)
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
    end
  }
  k = 0
  f.close
end

Thread.new {parseTxt 'sciezka_dostepu_do_pliku', :piano}