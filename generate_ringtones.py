from tones import SINE_WAVE, SAWTOOTH_WAVE, SQUARE_WAVE, TRIANGLE_WAVE
from tones.mixer import Mixer

SR = 44100

def make_ringtone(filename, melody, harmony, wave1=SAWTOOTH_WAVE, wave2=SINE_WAVE,
                  vibrato_freq=6.0, vibrato_var=20.0):
    mixer = Mixer(SR, 0.5)
    mixer.create_track(0, wave1, vibrato_frequency=vibrato_freq, vibrato_variance=vibrato_var, attack=0.05, decay=0.1)
    mixer.create_track(1, wave2, attack=0.05, decay=0.15)
    for _ in range(2):
        for (n, o, d), (hn, ho, hd) in zip(melody, harmony):
            mixer.add_note(0, note=n, octave=o, duration=d)
            mixer.add_note(1, note=hn, octave=ho, duration=hd)
    mixer.write_wav(filename)
    print(f"✅ {filename}")

# 1. Math — focused, precise, ascending
make_ringtone("math.wav",
    melody  = [('c',5,.2),('e',5,.2),('g',5,.2),('c',6,.4),('b',5,.2),('g',5,.4)],
    harmony = [('c',3,.2),('e',3,.2),('g',3,.2),('c',4,.4),('g',3,.2),('e',3,.4)])

# 2. Chores — upbeat, bouncy
make_ringtone("chores.wav",
    melody  = [('g',5,.15),('a',5,.15),('b',5,.15),('g',5,.15),('e',5,.3),('d',5,.15),('e',5,.35)],
    harmony = [('g',3,.15),('f',3,.15),('e',3,.15),('g',3,.15),('c',3,.3),('b',2,.15),('c',3,.35)],
    wave1=TRIANGLE_WAVE, vibrato_freq=5.0)

# 3. C++ — punchy, techy, square wave
make_ringtone("cpp.wav",
    melody  = [('e',5,.15),('e',5,.15),('g',5,.2),('f#',5,.15),('e',5,.15),('b',4,.4)],
    harmony = [('e',3,.15),('e',3,.15),('b',3,.2),('a',3,.15),('e',3,.15),('e',3,.4)],
    wave1=SQUARE_WAVE, wave2=SQUARE_WAVE, vibrato_freq=4.0, vibrato_var=10.0)

# 4. Kotlin — warm, smooth, modern
make_ringtone("kotlin.wav",
    melody  = [('a',5,.2),('g',5,.2),('e',5,.2),('f',5,.15),('g',5,.15),('a',5,.45)],
    harmony = [('f',4,.2),('e',4,.2),('c',4,.2),('d',4,.15),('e',4,.15),('f',4,.45)],
    wave1=SINE_WAVE, wave2=SINE_WAVE, vibrato_freq=7.0, vibrato_var=15.0)

# 5. Electronics — energetic, rising, sawtooth buzz
make_ringtone("electronics.wav",
    melody  = [('d',5,.15),('f',5,.15),('a',5,.15),('d',6,.3),('c',6,.15),('a',5,.4)],
    harmony = [('d',3,.15),('f',3,.15),('a',3,.15),('d',4,.3),('a',3,.15),('f',3,.4)],
    vibrato_freq=8.0, vibrato_var=25.0)

# 6. Electromagnetism — dramatic, deep, slow
make_ringtone("electromagnetism.wav",
    melody  = [('e',4,.3),('g',4,.3),('b',4,.3),('e',5,.5),('d',5,.3),('b',4,.5)],
    harmony = [('e',2,.3),('g',2,.3),('b',2,.3),('e',3,.5),('b',2,.3),('g',2,.5)],
    wave1=SAWTOOTH_WAVE, wave2=TRIANGLE_WAVE, vibrato_freq=3.0, vibrato_var=30.0)

# 7. Review/Buffer — gentle, winding down, calm
make_ringtone("review.wav",
    melody  = [('c',5,.25),('b',4,.25),('a',4,.25),('g',4,.25),('f',4,.25),('e',4,.5)],
    harmony = [('c',3,.25),('g',3,.25),('f',3,.25),('e',3,.25),('d',3,.25),('c',3,.5)],
    wave1=TRIANGLE_WAVE, wave2=SINE_WAVE, vibrato_freq=4.0, vibrato_var=12.0)

print("\n🎵 All 7 ringtones generated!")
