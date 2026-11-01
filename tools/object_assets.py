"""Build block textures only. The accepted GUI is deliberately not regenerated."""
from pathlib import Path
from PIL import Image,ImageDraw
from matte_finish import matte
p=Path(__file__).resolve().parents[1];base=p/'src/main/resources/assets/advancedapiary/textures'
master=Image.open(p/'tools/art/gui-materials.png').convert('RGBA');mw,mh=master.size
materials={'wood':master.crop((4,4,mw//2-4,mh//2-4))}
for name in ['front','side','top']:
 b=materials['wood'].resize((64,64),Image.Resampling.NEAREST);bd=ImageDraw.Draw(b)
 for x in [2,58]:
  bd.rectangle((x,0,x+3,63),fill='#b89453');bd.line((x,0,x,63),fill='#efcf8a');bd.line((x+3,0,x+3,63),fill='#4c3623')
 for y in [2,30,60]:bd.line((0,y,63,y),fill='#422e21',width=2);bd.line((0,y+2,63,y+2),fill='#b98d52')
 if name=='front':
  for y in [11,37]:
   for x in [10,26,42]:
    bd.rounded_rectangle((x-2,y-2,x+12,y+16),3,fill='#c79f56',outline='#f0d293',width=1)
    bd.rectangle((x,y,x+10,y+12),fill='#182e27');bd.rectangle((x+2,y+4,x+8,y+9),fill='#986e2d');bd.line((x+2,y+4,x+8,y+4),fill='#edce78')
 elif name=='side':
  bd.rounded_rectangle((10,10,53,53),5,fill='#ad8348',outline='#e7c78d',width=2);bd.rectangle((14,14,49,49),fill='#182e27')
  for y in range(18,48,6):bd.line((18,y,45,y),fill='#aa8b50',width=2);bd.line((18,y+2,45,y+2),fill='#0b1917')
 else:
  bd.rounded_rectangle((12,10,51,53),7,fill='#c8a261',outline='#eed69b',width=2);bd.rounded_rectangle((16,14,47,49),5,fill='#24392d')
  for y in range(21,47,10):
   for x in range(22,46,12):bd.regular_polygon((x,y,6),6,fill='#a67c35',outline='#e4bf6d')
 for x in [4,60]:
  for y in [4,59]:bd.ellipse((x-1,y-1,x+1,y+1),fill='#efcf8a');bd.point((x,y),fill='#493422')
 b=matte(b.resize((32,32),Image.Resampling.LANCZOS),32).resize((64,64),Image.Resampling.NEAREST)
 b.save(base/'blocks'/('apiary_'+name+'.png'))
