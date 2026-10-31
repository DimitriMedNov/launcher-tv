// Íconos vectoriales simples (formas + degradados) para pasar a VectorDrawable / ImageVector.
export function makeIcons(React) {
  const h = React.createElement;
  const CLOUD = 'M12 30 H38 A9 9 0 0 0 38 12 A13 13 0 0 0 13.5 8.5 A11 11 0 0 0 12 30 Z';

  const grad = (id, c1, c2, radial) => radial
    ? h('radialGradient', {id, cx:'38%', cy:'35%', r:'75%'}, h('stop', {offset:'0', stopColor:c1}), h('stop', {offset:'1', stopColor:c2}))
    : h('linearGradient', {id, x1:'0', y1:'0', x2:'0', y2:'1'}, h('stop', {offset:'0', stopColor:c1}), h('stop', {offset:'1', stopColor:c2}));

  function weather(kind, size, rm, uid) {
    uid = uid || 'w';
    const an = (a) => rm ? 'none' : a;
    const id = (n) => `${uid}-${n}`;
    const defs = [
      grad(id('sun'), '#FFD60A', '#FF9F0A', true),
      grad(id('heat'), '#FF9F0A', '#FF453A', true),
      grad(id('white'), '#FFFFFF', '#E5E5EA'),
      grad(id('light'), '#F2F2F7', '#D1D1D6'),
      grad(id('gray'), '#AEAEB2', '#8E8E93'),
      grad(id('dark'), '#636366', '#48484A'),
      grad(id('moon'), '#F2F7FF', '#B5C8E8', true),
    ];
    const sun = (cx, cy, r, fill, rayColor) => {
      const rays = [];
      for (let i = 0; i < 8; i++) {
        const a = i * Math.PI / 4, c = Math.cos(a), s = Math.sin(a);
        rays.push(h('line', {key:i, x1:cx + c*(r+4), y1:cy + s*(r+4), x2:cx + c*(r+9), y2:cy + s*(r+9), stroke:rayColor, strokeWidth:3.2, strokeLinecap:'round'}));
      }
      return h('g', {key:'sun'},
        h('g', {style:{animation:an('wxSway 4s ease-in-out infinite alternate'), transformBox:'fill-box', transformOrigin:'center'}}, rays),
        h('circle', {cx, cy, r, fill:`url(#${id(fill)})`}));
    };
    const cloud = (x, y, s, fill, delay) => h('g', {key:'c'+x+y, transform:`translate(${x} ${y}) scale(${s})`},
      h('path', {d:CLOUD, fill:`url(#${id(fill)})`, style:{animation:an(`wxDrift 3s ease-in-out ${delay||0}s infinite alternate`)}}));
    const drop = (x, y, len, delay, w) => h('line', {key:'d'+x, x1:x, y1:y, x2:x - len*0.35, y2:y + len, stroke:'#0A84FF', strokeWidth:w||3.2, strokeLinecap:'round', style:{animation:an(`wxFall 1.6s ease-in ${delay}s infinite`)}});
    const moon = (cx, cy, r) => h('g', {key:'moon'},
      h('mask', {id:id('mm')}, h('rect', {x:0, y:0, width:64, height:64, fill:'#fff'}), h('circle', {cx:cx + r*0.6, cy:cy - r*0.4, r:r*0.85, fill:'#000'})),
      h('circle', {cx, cy, r, fill:`url(#${id('moon')})`, mask:`url(#${id('mm')})`}));
    const star = (x, y, s, delay) => h('polygon', {key:'s'+x, points:`${x},${y-s} ${x+s*.3},${y-s*.3} ${x+s},${y} ${x+s*.3},${y+s*.3} ${x},${y+s} ${x-s*.3},${y+s*.3} ${x-s},${y} ${x-s*.3},${y-s*.3}`, fill:'#F2F7FF', style:{animation:an(`wxTwinkle 3s ease-in-out ${delay}s infinite alternate`)}});
    const wave = (y, color, delay) => h('path', {key:'wv'+y, d:`M14 ${y} q4 -4 8 0 t8 0 t8 0 t8 0`, stroke:color, strokeWidth:2.6, fill:'none', strokeLinecap:'round', style:{animation:an(`wxDrift 3s ease-in-out ${delay}s infinite alternate`)}});
    const wind = (d, delay) => h('path', {key:d, d, stroke:'#FFFFFF', strokeWidth:3.5, fill:'none', strokeLinecap:'round', style:{animation:an(`wxDrift 2.5s ease-in-out ${delay}s infinite alternate`)}});
    const fogLine = (y, x1, x2, op, delay) => h('line', {key:'f'+y, x1, y1:y, x2, y2:y, stroke:'#AEAEB2', strokeWidth:4.5, strokeLinecap:'round', opacity:op, style:{animation:an(`wxDrift 3.5s ease-in-out ${delay}s infinite alternate`)}});
    let body = [];
    switch (kind) {
      case 'sunny': body = [sun(32, 32, 14, 'sun', '#FFB340')]; break;
      case 'partly': body = [sun(24, 24, 11, 'sun', '#FFB340'), cloud(14, 26, .95, 'white', 0)]; break;
      case 'cloudy': body = [cloud(18, 10, .82, 'gray', .8), cloud(8, 24, .95, 'white', 0)]; break;
      case 'rain': body = [cloud(8, 6, 1, 'gray', 0), drop(22, 44, 9, 0), drop(32, 44, 9, .45), drop(42, 44, 9, .9)]; break;
      case 'storm': body = [cloud(8, 4, 1, 'dark', 0), drop(18, 42, 8, .2), drop(46, 42, 8, .7),
        h('polygon', {key:'b', points:'34 34 25 50 31 50 29 61 41 44 35 44 38 34', fill:'#FFD60A', style:{animation:an('wxFlash 6s ease-in-out infinite')}})]; break;
      case 'drizzle': body = [cloud(8, 6, 1, 'light', 0), drop(20, 44, 5, 0, 2.6), drop(32, 47, 5, .5, 2.6), drop(44, 44, 5, 1, 2.6)]; break;
      case 'clearNight': body = [moon(30, 32, 15), star(51, 16, 3, 0), star(55, 31, 2, 1.2)]; break;
      case 'cloudyNight': body = [moon(24, 22, 11), cloud(14, 26, .95, 'white', 0)]; break;
      case 'heat': body = [sun(32, 25, 12, 'heat', '#FF6B3D'), wave(50, '#FF9F0A', 0), wave(57, '#FF9F0A', .6)]; break;
      case 'wind': body = [wind('M8 24 H36 a5 5 0 1 0 -5 -5', 0), wind('M8 34 H48 a5 5 0 1 1 -5 5', .5), wind('M8 44 H30 a4 4 0 1 1 -4 4', 1)]; break;
      case 'hurricane': body = [h('path', {key:'sp', d:'M32 32 a4 4 0 1 1 4 4 a8 8 0 1 1 -8 -8 a12 12 0 1 1 12 12 a16 16 0 1 1 -16 -16', stroke:'#FF453A', strokeWidth:3.5, fill:'none', strokeLinecap:'round', style:{animation:an('wxSpin 4s linear infinite'), transformBox:'fill-box', transformOrigin:'center'}}),
        wind('M4 54 H20', 0)]; break;
      case 'fog': body = [fogLine(22, 14, 50, .95, 0), fogLine(32, 8, 56, .75, .4), fogLine(42, 12, 52, .55, .8), fogLine(52, 18, 46, .4, 1.2)]; break;
    }
    return h('svg', {width:size, height:size, viewBox:'0 0 64 64', style:{display:'block', overflow:'visible'}, 'aria-label':kind}, h('defs', null, defs), body);
  }

  function input(kind, size, opts) {
    opts = opts || {};
    const c = opts.color || '#FFFFFF';
    const st = {stroke:c, strokeWidth:3.5, fill:'none', strokeLinecap:'round', strokeLinejoin:'round'};
    let body = [];
    switch (kind) {
      case 'console': body = [
        h('path', {key:'b', d:'M18 22 H46 A10 10 0 0 1 56 32 L54 44 A6 6 0 0 1 43 46 L40 40 H24 L21 46 A6 6 0 0 1 10 44 L8 32 A10 10 0 0 1 18 22 Z', ...st}),
        h('line', {key:'d1', x1:19, y1:32, x2:29, y2:32, ...st}), h('line', {key:'d2', x1:24, y1:27, x2:24, y2:37, ...st}),
        h('circle', {key:'a', cx:41, cy:29, r:2.4, fill:c}), h('circle', {key:'b2', cx:47, cy:34, r:2.4, fill:c})]; break;
      case 'stb': body = [
        h('rect', {key:'r', x:10, y:32, width:44, height:18, rx:4, ...st}),
        h('line', {key:'a1', x1:24, y1:32, x2:18, y2:16, ...st}), h('line', {key:'a2', x1:40, y1:32, x2:46, y2:16, ...st}),
        h('line', {key:'l', x1:17, y1:41, x2:32, y2:41, ...st}), h('circle', {key:'c', cx:46, cy:41, r:2.4, fill:c})]; break;
      case 'laptop': body = [
        h('rect', {key:'s', x:14, y:16, width:36, height:24, rx:3, ...st}),
        h('path', {key:'b', d:'M8 46 H56 L52 51 H12 Z', fill:c})]; break;
      case 'soundbar': body = [
        h('rect', {key:'r', x:6, y:26, width:52, height:14, rx:7, ...st}),
        ...[18, 26, 38, 46].map(x => h('circle', {key:'c'+x, cx:x, cy:33, r:2.2, fill:c}))]; break;
      case 'player': body = [
        h('rect', {key:'r', x:8, y:22, width:48, height:22, rx:4, ...st}),
        h('circle', {key:'d', cx:24, cy:33, r:6, ...st}), h('circle', {key:'p', cx:24, cy:33, r:1.6, fill:c}),
        h('line', {key:'l1', x1:36, y1:30, x2:48, y2:30, ...st}), h('line', {key:'l2', x1:36, y1:36, x2:44, y2:36, ...st})]; break;
      case 'hdmi': body = [
        h('g', {key:'g', transform:'translate(32 20) scale(.7) translate(-32 -32)'},
          h('path', {d:'M12 24 H52 V36 L46 42 H18 L12 36 Z', ...st}),
          ...[22, 28, 34, 40].map(x => h('line', {key:'p'+x, x1:x, y1:30, x2:x, y2:34, ...st}))),
        opts.num != null ? h('text', {key:'t', x:32, y:58, textAnchor:'middle', fontFamily:'Inter, Helvetica, sans-serif', fontWeight:700, fontSize:28, fill:c}, String(opts.num)) : null]; break;
      case 'antenna': body = [
        h('rect', {key:'b', x:20, y:36, width:24, height:18, rx:3, ...st}),
        h('line', {key:'e1', x1:28, y1:36, x2:16, y2:16, ...st}), h('line', {key:'e2', x1:36, y1:36, x2:46, y2:16, ...st}),
        h('path', {key:'w1', d:'M49.5 7.9 A7 7 0 0 1 52.1 17.5', ...st, strokeWidth:2.8}),
        h('path', {key:'w2', d:'M52 3.6 A12 12 0 0 1 56.4 20', ...st, strokeWidth:2.8, opacity:.6})]; break;
    }
    return h('svg', {width:size, height:size, viewBox:'0 0 64 64', style:{display:'block', overflow:'visible'}, 'aria-label':kind}, body);
  }

  return {weather, input};
}
