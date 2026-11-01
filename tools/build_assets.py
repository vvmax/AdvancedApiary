"""Build block/item textures and previews; preserve the accepted GUI texture."""
import object_assets
import module_art
from pathlib import Path
from PIL import Image,ImageDraw
p=Path(__file__).resolve().parents[1];base=p/'src/main/resources/assets/advancedapiary/textures'
# This example uses the actual native slot coordinates and the shipped item textures.
demo=Image.open(p/'gui-preview-unlocked.png').convert('RGBA')
for i,name in enumerate(['garden','speed','productivity','parallel','climate','rain','light','sky']):
 tex=Image.open(base/('items/module_'+name+'.png')).convert('RGBA')
 demo.alpha_composite(tex,((89+18*i)*4,135*4))
demo.save(p/'gui-style-preview.png')
preview=Image.new('RGB',(680,460),'#292725');preview.paste(Image.open(p/'modules-preview.png'),(0,0));d=ImageDraw.Draw(preview)
for i,name in enumerate(['front','side','top']):
 tex=Image.open(base/('blocks/apiary_'+name+'.png'));preview.paste(tex,(10+i*100,370),tex);d.text((10+i*100,438),name,fill='#e4ce9b')
preview.save(p/'assets-preview.png')
