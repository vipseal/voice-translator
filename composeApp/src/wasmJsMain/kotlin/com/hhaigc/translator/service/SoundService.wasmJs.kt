package com.hhaigc.translator.service

@JsFun("""
() => {
    if (!window.__audioCtx) {
        window.__audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (window.__audioCtx.state === 'suspended') {
        window.__audioCtx.resume();
    }
    return window.__audioCtx;
}
""")
private external fun jsGetAudioCtx(): JsAny

@JsFun("""
() => {
    try {
        const ctx = window.__audioCtx || new (window.AudioContext || window.webkitAudioContext)();
        window.__audioCtx = ctx;
        if (ctx.state === 'suspended') ctx.resume();
        const t = ctx.currentTime;
        for (let i = 0; i < 2; i++) {
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.frequency.value = i === 0 ? 800 : 1200;
            osc.type = 'sine';
            gain.gain.setValueAtTime(0.3, t + i * 0.08);
            gain.gain.exponentialRampToValueAtTime(0.001, t + i * 0.08 + 0.08);
            osc.start(t + i * 0.08);
            osc.stop(t + i * 0.08 + 0.08);
        }
    } catch(e) {}
}
""")
private external fun jsPlayStartSound()

@JsFun("""
() => {
    try {
        const ctx = window.__audioCtx || new (window.AudioContext || window.webkitAudioContext)();
        window.__audioCtx = ctx;
        if (ctx.state === 'suspended') ctx.resume();
        const t = ctx.currentTime;
        for (let i = 0; i < 2; i++) {
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.frequency.value = i === 0 ? 1200 : 800;
            osc.type = 'sine';
            gain.gain.setValueAtTime(0.3, t + i * 0.08);
            gain.gain.exponentialRampToValueAtTime(0.001, t + i * 0.08 + 0.08);
            osc.start(t + i * 0.08);
            osc.stop(t + i * 0.08 + 0.08);
        }
    } catch(e) {}
}
""")
private external fun jsPlayStopSound()

@JsFun("""
() => {
    try {
        const ctx = window.__audioCtx || new (window.AudioContext || window.webkitAudioContext)();
        window.__audioCtx = ctx;
        if (ctx.state === 'suspended') ctx.resume();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = 600;
        osc.type = 'sine';
        gain.gain.value = 0.2;
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.05);
        osc.start();
        osc.stop(ctx.currentTime + 0.05);
    } catch(e) {}
}
""")
private external fun jsPlayClick()

actual class SoundService {
    actual fun playStartRecording() {
        jsPlayStartSound()
    }
    actual fun playStopRecording() {
        jsPlayStopSound()
    }
    actual fun playClick() {
        jsPlayClick()
    }
}
