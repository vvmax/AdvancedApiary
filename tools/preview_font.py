"""Preview helper using Minecraft's Unicode glyph atlas and advances, not a system font.
The game assets are read locally and are not copied into the project or mod JAR.
"""
import os
from pathlib import Path
from PIL import Image, ImageColor
class GameFont:
    def __init__(self):
        self.root=Path(os.environ.get('MC1710_FONT_ASSETS','/workspace/minecraft-1.7.10/examples/forge-mod/build/rfg/minecraft-src/resources/assets/minecraft'))
        self.widths=(self.root/'font/glyph_sizes.bin').read_bytes();self.pages={}
    def advance(self,ch):
        if ch==' ':return 4
        b=self.widths[ord(ch)]
        if not b:return 0
        lo=b>>4;hi=b&15
        if hi>7:lo=0;hi=15
        return (hi+1-lo)//2+1
    def width(self,text):return sum(self.advance(ch) for ch in text)
    def draw(self,image,text,box,color,dx=0,dy=0):
        x,y,w,h,preferred,center=box
        scale=min(preferred,w/max(1,self.width(text)),h/9)
        x+=dx+(w-self.width(text)*scale)/2 if center else dx
        y+=dy+(h-9*scale)/2
        for ch in text:
            code=ord(ch);b=self.widths[code]
            if b and ch!=' ':
                page=code//256
                if page not in self.pages:self.pages[page]=Image.open(self.root/f'textures/font/unicode_page_{page:02x}.png').convert('RGBA')
                lo=b>>4;hi=(b&15)+1;px=(code%16)*16;py=((code&255)//16)*16
                glyph=self.pages[page].crop((px+lo,py,px+hi,py+16))
                tinted=Image.new('RGBA',glyph.size,ImageColor.getrgb(color)+(255,));tinted.putalpha(glyph.getchannel('A'))
                tinted=tinted.resize((max(1,round(glyph.width*scale*2)),max(1,round(32*scale))),Image.Resampling.NEAREST)
                image.alpha_composite(tinted,(round(x*4),round(y*4)))
            x+=self.advance(ch)*scale
