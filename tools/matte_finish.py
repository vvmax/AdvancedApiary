"""Object-only finish matched to the accepted wood/wax GUI; never edits GUI pixels."""
from PIL import Image,ImageEnhance

def matte(im,colors=32):
 im=im.convert('RGBA');alpha=im.getchannel('A')
 # Shared compressed contrast and restrained color keep symbols readable without glow.
 rgb=ImageEnhance.Color(im.convert('RGB')).enhance(.62)
 rgb=ImageEnhance.Contrast(rgb).enhance(.68)
 rgb=Image.blend(rgb,Image.new('RGB',rgb.size,'#ad905b'),.14)
 # A coarse, deliberate pixel grid matches the original interface rather than smooth metal.
 rgb=rgb.quantize(colors=colors,method=Image.Quantize.MEDIANCUT).convert('RGBA')
 rgb.putalpha(alpha);return rgb
