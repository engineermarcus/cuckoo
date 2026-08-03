from tones import SINE_WAVE, SAWTOOTH_WAVE
from tones.mixer import Mixer

mixer = Mixer(44100, 0.5)

# Track 0 - melody (sawtooth = warm, buzzy, phone-like)
mixer.create_track(0, SAWTOOTH_WAVE, vibrato_frequency=6.0, vibrato_variance=20.0, attack=0.05, decay=0.1)

# Track 1 - harmony underneath (sine = smooth backing)
mixer.create_track(1, SINE_WAVE, attack=0.05, decay=0.15)

# Melody — a little 8-note hook that loops nicely
notes = [
    ('e', 5, 0.18), ('g', 5, 0.18), ('a', 5, 0.25),
    ('e', 5, 0.18), ('g', 5, 0.18), ('b', 5, 0.35),
    ('a', 5, 0.18), ('g', 5, 0.45),
]

harmony = [
    ('c', 4, 0.18), ('c', 4, 0.18), ('f', 4, 0.25),
    ('c', 4, 0.18), ('c', 4, 0.18), ('g', 4, 0.35),
    ('f', 4, 0.18), ('e', 4, 0.45),
]

# Play it twice for a proper ringtone loop
for _ in range(2):
    for (n, oct, dur), (hn, hoct, hdur) in zip(notes, harmony):
        mixer.add_note(0, note=n, octave=oct, duration=dur)
        mixer.add_note(1, note=hn, octave=hoct, duration=hdur)

mixer.write_wav('cuckoo.wav')
print("cuckoo.wav ready 🐦")
