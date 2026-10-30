from pathlib import Path
from PIL import Image, ImageDraw
import json
p=Path(__file__).resolve().parents[1];base=p/'src/main/resources/assets/advancedapiary/textures'
(base/'gui').mkdir(parents=True,exist_ok=True);(base/'blocks').mkdir(exist_ok=True);(base/'items').mkdir(exist_ok=True)
im=Image.new('RGBA',(256,256));d=ImageDraw.Draw(im)
def rect(x,y,w,h,c):
 if w>0 and h>0:d.rectangle((x,y,x+w-1,y+h-1),fill=c)
def panel(x,y,w,h,fill,edge='#b99a61',r=5):
 d.rounded_rectangle((x,y,x+w-1,y+h-1),r,fill=edge);d.rounded_rectangle((x+1,y+1,x+w-2,y+h-2),max(1,r-1),fill=fill)
def hexagon(x,y,c):d.line([(x+2,y),(x+5,y),(x+7,y+3),(x+5,y+6),(x+2,y+6),(x,y+3),(x+2,y)],fill=c)
panel(0,0,248,238,'#795134','#392a21',11);panel(2,2,244,234,'#94673e','#d6b273',9)
for y in range(5,234,7):
 for x in range(5,243,24):d.line((x,y,x+9+(x*7+y)%11,y),fill='#8b603b')
panel(5,4,238,14,'#44372b','#b99960',6)
slots=[]
def slot(x,y,group):
 rect(x,y,18,18,'#71664e');rect(x+1,y+1,17,17,'#f1ddb0');rect(x+1,y+1,16,16,'#83785a');rect(x+2,y+2,15,15,'#918566')
 slots.append(dict(x=x+1,y=y+1,group=group))
 if group=='queen':d.line([(x+5,y+7),(x+6,y+12),(x+12,y+12),(x+13,y+7),(x+10,y+9),(x+9,y+5),(x+7,y+9),(x+5,y+7)],fill='#d3b967')
 if group=='module':hexagon(x+5,y+5,'#b4a174')
for i in range(6):
 x=8+i%3*56;y=24+i//3*48
 panel(x,y,50,45,'#e4ce9b','#ad905b',5)
 d.rounded_rectangle((x+2,y+2,x+47,y+10),3,fill='#79603a')
 slot(x+4,y+14,'queen');panel(x+3,y+36,44,6,'#736347','#b69b60',2)
 # Small honeycomb port next to status, distinct from inventory socket.
 hexagon(x+34,y+16,'#c1a164')
panel(178,21,62,103,'#d3bd8b','#ad905b',5)
# Slots appended in container order: queens, internal plants, modules, products, player.
for i in range(4):slot(10+i*18,134,'flowers')
for i in range(8):slot(88+i*18,134,'module')
for row in range(5):
 for col in range(3):slot(182+18*col,32+row*18,'output')
panel(37,159,174,78,'#b8a078','#c8b184',5)
for row in range(3):
 for col in range(9):slot(42+18*col,159+row*18,'inventory')
for col in range(9):slot(42+18*col,217,'hotbar')
for yy,c in enumerate(['#f7dda1','#d5a145','#d5a145','#8e622c']):rect(0,240+yy,42,1,c)
d.line([(0,249),(1,250),(4,247)],fill='#b6d68d');rect(8,247,1,3,'#ffcb6a');rect(8,251,1,1,'#ffcb6a');rect(14,249,4,1,'#c7bc9c')
im.save(base/'gui/advanced_apiary.png')
assert len(slots)==69
for i,a in enumerate(slots):
 for b in slots[i+1:]:assert not(a['x']<b['x']+16 and b['x']<a['x']+16 and a['y']<b['y']+16 and b['y']<a['y']+16)
(p/'gui-layout.json').write_text(json.dumps(dict(width=248,height=238,slots=slots),indent=2))
labels=json.loads((p/'tools/gui-labels.json').read_text())
java=['package ru.advancedapiary.client;','/** Generated from tools/gui-labels.json. */','public final class GuiTextLayout {']
for name,box in labels.items():
 values=','.join(str(float(v))+'F' for v in box[:5])+','+('1F' if box[5] else '0F')
 java.append('    public static final float[] '+name+'={'+values+'};')
java.append('    private GuiTextLayout(){}\n}')
(p/'src/main/java/ru/advancedapiary/client/GuiTextLayout.java').write_text('\n'.join(java)+'\n')
from preview_font import GameFont
font=GameFont()
preview=im.crop((0,0,248,238)).resize((992,952),Image.Resampling.NEAREST)
font.draw(preview,'Изолированная пасека',labels['TITLE'],'#f4dfb4')
font.draw(preview,'Работают: 0 / 6',labels['COUNTER'],'#f4dfb4')
font.draw(preview,'Продукция',labels['PRODUCTS'],'#493a25')
for i in range(6):font.draw(preview,'Матка '+str(i+1),labels['QUEEN'],'#fff0c9',(i%3)*56,(i//3)*48)
font.draw(preview,'Растения',labels['PLANTS'],'#f4dfb4')
font.draw(preview,'Модули улучшений · 8',labels['MODULES'],'#f4dfb4')
font.draw(preview,'Растения не расходуются. Каждой матке нужен подходящий вид.',labels['NOTE'],'#f4dfb4')
preview.save(p/'gui-preview-unlocked.png')
locked=preview.copy();ld=ImageDraw.Draw(locked)
for i in range(4):
 x=10+i*18;y=134
 def lr(a,b,w,h,c):ld.rectangle(((x+a)*4,(y+b)*4,(x+a+w)*4-1,(y+b+h)*4-1),fill=c)
 lr(1,1,16,16,'#625c49');lr(6,6,6,2,'#d6bb7c');lr(5,8,8,5,'#d6bb7c');lr(7,9,3,2,'#625c49')
locked.paste(im.crop((10,153,238,159)).resize((912,24),Image.Resampling.NEAREST),(40,612))
font.draw(locked,'Слоты растений открываются модулем внутреннего сада.',labels['NOTE'],'#f4dfb4')
locked.save(p/'gui-preview.png')
# Original 16px block textures: brass sealed panels and six small chamber ports.
for name in ['apiary_front','apiary_side','apiary_top']:
 t=Image.new('RGBA',(16,16),'#815534');td=ImageDraw.Draw(t)
 for y in [0,5,10,15]:td.line((0,y,15,y),fill='#543d2b')
 td.rectangle((1,1,14,14),outline='#c7a45c')
 if name=='apiary_front':
  for y in [4,9]:
   for x in [3,7,11]:td.rectangle((x,y,x+2,y+2),fill='#3e3a2a');td.point((x,y),fill='#e9bd64')
 elif name=='apiary_top':
  td.rectangle((4,4,11,11),fill='#c89a44');td.rectangle((6,6,9,9),fill='#715733')
 else:
  td.rectangle((3,4,12,11),fill='#a67d40');td.line((4,7,11,7),fill='#694b2d')
 t.save(base/'blocks'/f'{name}.png')
t=Image.new('RGBA',(16,16));td=ImageDraw.Draw(t);td.rectangle((2,2,13,13),fill='#3f4d43',outline='#cfaa59');td.rectangle((5,5,10,10),fill='#e1ba59');td.rectangle((7,4,8,11),fill='#f5dda2');t.save(base/'items/module.png')
print('GUI: 6 queens, 4 internal plants, 8 modules, 15 output, 36 player slots validated')

# Distinct item textures per module type.
for name,color,symbol in [('speed','#7ab8db','arrow'),('productivity','#eac65a','plus'),('specialty','#c791e4','diamond'),('parallel','#88c398','parallel'),('export','#d99e70','export'),('focus','#d792c4','target'),('sorter','#b5a8e2','sort'),('heater','#e6965f','heat'),('cooler','#8fcce0','cold'),('humidifier','#77badb','drop'),('dryer','#d1bb70','dry'),('rain','#72aebe','roof'),('light','#edda78','sun'),('dark','#a5a0cc','moon'),('sky','#95c8df','sky'),('climate','#b9d1a5','control'),('garden','#99cf74','plant')]:
 t=Image.new('RGBA',(16,16));td=ImageDraw.Draw(t)
 td.rounded_rectangle((1,1,14,14),2,fill='#435246',outline='#d6b46c');td.rectangle((3,3,12,12),fill='#26382f')
 if symbol=='arrow':td.polygon([(5,4),(11,8),(5,11),(7,8)],fill=color)
 elif symbol=='plus':td.rectangle((7,4,8,11),fill=color);td.rectangle((4,7,11,8),fill=color)
 elif symbol=='diamond':td.polygon([(8,3),(12,8),(8,12),(4,8)],fill=color)
 elif symbol=='parallel':td.rectangle((4,4,6,11),fill=color);td.rectangle((9,4,11,11),fill=color)
 elif symbol=='export':td.line((4,7,11,7),fill=color,width=2);td.polygon([(9,4),(12,7),(9,10)],fill=color);td.line((4,4,4,11),fill=color)
 elif symbol=='target':td.ellipse((4,4,11,11),outline=color);td.rectangle((7,7,8,8),fill=color)
 elif symbol=='sort':
  for n in range(3):td.line((4,5+3*n,6+2*n,5+3*n),fill=color)

 elif symbol=='heat':td.polygon([(7,3),(11,8),(10,11),(6,12),(4,9),(6,7)],fill=color)
 elif symbol=='cold':td.line((7,3,7,12),fill=color);td.line((3,7,12,7),fill=color);td.line((4,4,11,11),fill=color);td.line((4,11,11,4),fill=color)
 elif symbol=='drop':td.polygon([(8,3),(4,9),(5,11),(10,11),(11,9)],fill=color)
 elif symbol=='dry':td.line((4,5,11,5),fill=color);td.line((4,8,11,8),fill=color);td.line((4,11,11,11),fill=color)
 elif symbol=='roof':td.line((3,7,8,3,12,7),fill=color,width=2);td.line((5,9,5,12),fill=color);td.line((10,9,10,12),fill=color)
 elif symbol=='sun':td.ellipse((5,5,10,10),fill=color);td.line((3,3,4,4),fill=color);td.line((12,3,11,4),fill=color);td.line((3,12,4,11),fill=color);td.line((12,12,11,11),fill=color)
 elif symbol=='moon':td.ellipse((4,3,11,12),fill=color);td.ellipse((7,2,13,9),fill='#26382f')
 elif symbol=='sky':td.rectangle((4,4,11,11),outline=color);td.line((5,6,10,6),fill=color)
 elif symbol=='control':td.line((4,5,11,5),fill=color);td.line((4,10,11,10),fill=color);td.rectangle((5,4,6,6),fill=color);td.rectangle((9,9,10,11),fill=color)
 elif symbol=='plant':td.line((8,5,8,12),fill=color);td.ellipse((3,4,7,7),fill=color);td.ellipse((8,3,12,6),fill=color);td.line((5,12,11,12),fill='#d3a76b')
 t.save(base/'items'/('module_'+name+'.png'))

opened=Image.open(p/'gui-preview-unlocked.png').convert('RGBA')
garden_icon=Image.open(base/'items/module_garden.png').convert('RGBA').resize((64,64),Image.Resampling.NEAREST)
opened.alpha_composite(garden_icon,(89*4,135*4))
opened.save(p/'gui-preview-unlocked.png')

# Final high resolution resources.
import refine_assets
