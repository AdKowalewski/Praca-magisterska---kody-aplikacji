from music import *
import sys

#instrument = ELECTRIC_GUITAR
#instrument = PIANO
#tablica symboli nut
noteLetters = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", 
  "Bb", "B"]
#tablica numerów oktaw
octaves = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
#słownik długości nut
durationsDict = {
   "1": WN,
   "2": HN,
   "4": QN,
   "8": EN,
   "6": SN
}

#funkcja do tworzenia ścieżki muzycznej na podstawie
#pliku z zapisem muzycznym oraz instrumentu
def parseTxt(instrument, filepath):
   #wczytywanie pliku
   f = open(filepath, 'r')
   #lista temp (przydatna, jeśli utwór posiada zmienne tempo)
   temposList = []
   #lista wysokości nut
   pitches = []
   #lista długości nut
   durations = []
   #lista fraz; ile zmian tempa, tyle będzie fraz
   phraseList = []
   #utworzenie bazowej frazy
   phrase = Phrase(0.0)
   #ustawienie instrumentu
   phrase.setInstrument(instrument)
   #mapa wysokości nut we fragmencie
   dictOfFragmentPitches = {}
   #mapa długości nut we fragmencie
   dictOfFragmentDurations = {}
   #stan, czy zapamiętywany jest fragment
   isFragment = 0
   #lista wysokości nut w zapamiętywanym fragmencie
   fragmentPitches = []
   #lista długości nut w zapamiętywanym fragmencie
   fragmentDurations = []
   #iterator
   k = 0
   #wczytywanie linii
   while True:
      line = f.readline()
      #inkrementacja iteratora
      k = k + 1
      #jeśli brak linii, wówczas zapamiętywanie ścieżki,
      #przerwanie pętli i opróżnianie list
      if not line:
         phrase.addNoteList(pitches, durations)
         phraseList.append(phrase)
         pitches = []
         durations = []
         break
      #ignorowanie linii, jeśli napotkano
      #znak "#" lub "."
      if line[0] == "#":
         continue
      if line[0] == ".":
         print("End of the song")
         break
      #podział linii względem ","
      parameters = line.split(",")
      if len(parameters) == 2:
         #ustawienie tempa
         if parameters[0] == "T":
            if int(parameters[1]) < 0:
               print("Tempo in line {} must be greater than 0!".format(k))
               break
            else:
               temposList.append(int(parameters[1]))
               if len(temposList) < 2:
                  phrase.setTempo(temposList[len(temposList)-1])  
               if len(temposList) >= 2:
                  phrase.addNoteList(pitches, durations)
                  endTime = phrase.getEndTime()
                  phraseList.append(phrase)
                  phrase = Phrase(temposList[len(temposList)-1]*endTime/temposList[len(temposList)-2])
                  phrase.setInstrument(instrument)
                  phrase.setTempo(temposList[len(temposList)-1])
                  pitches = []
                  durations = []
         #dodanie pauzy
         if parameters[0] == "R":
            pitches.append(REST)
            if isFragment == 1:
               fragmentPitches.append(REST)
            if line.endswith("1/32"):
               durations.append(TN)
               if isFragment == 1:
                 fragmentDurations.append(TN)
            for x, y in durationsDict.items():
               if line[:-1].endswith(x):
                  durations.append(y)
                  if isFragment == 1:
                     fragmentDurations.append(y)
         #rozpoczęcie zapamiętywania fragmentu
         if parameters[0] == "Fragment":
            isFragment = 1
            titleOfFragment = parameters[1]
         #zakończenie zapamiętywania fragmentu
         if parameters[0] == "End":
            dictOfFragmentPitches[titleOfFragment] = fragmentPitches
            dictOfFragmentDurations[titleOfFragment] = fragmentDurations
            isFragment = 0
            fragmentPitches = []
            fragmentDurations = []
         #wywołanie fragmentu
         if parameters[0] == "Call":
            if parameters[1] not in dictOfFragmentPitches.values() or parameters[1] not in dictOfFragmentDurations.values():
               print("Fragment of title {} in line {} does not exist!".format(parameters[1], k))
               break
            else:
               pitches.extend(dictOfFragmentPitches[parameters[1]])
               durations.extend(dictOfFragmentDurations[parameters[1]])
      #dodanie nowej nuty
      elif len(parameters) == 3:
         for n in noteLetters:
            if parameters[0] == n:
               for o in octaves:
                  if parameters[1] == o:
                     pitches.append(12 * int(octaves.index(o)) + int(noteLetters.index(n)))
                     if isFragment == 1:
                        fragmentPitches.append(12 * int(octaves.index(o)) + int(noteLetters.index(n)))
         if line.endswith("1/32"):
            durations.append(TN)
            if isFragment == 1:
               fragmentDurations.append(TN)
         for x, y in durationsDict.items():
            if line[:-1].endswith(x):
               durations.append(y)
               if isFragment == 1:
                  fragmentDurations.append(y)
      else:
         print("Invalid number of parameters in line {}!".format(k))
   k = 0
   f.close()
   return phraseList


phraseList = parseTxt(int(sys.argv[1]), sys.argv[2])
#phraseList = parseTxt('sciezka_dostepu_do_pliku', 0)
#w przypadku większej ilości plików:
#
#phraseList1 = parseTxt('plik1', 0)
#phraseList2 = parseTxt('plik2', 0)
#
#part = Part()
#for p in phraseList1:
#   part.addPhrase(p)
#for p in phraseList2:
#   part.addPhrase(p)

part = Part()
for p in phraseList:
   part.addPhrase(p)
   
#odtworzenie utworu
Play.midi(part)

#zapis do pliku MIDI
Write.midi(part, "plik.midi")