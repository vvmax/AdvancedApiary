"""Exact enumeration of all productive module allocations using the Java production formula.
chance is the base species chance times genetic speed times config multiplier.
Reports expected items/minute for ONE product entry, including its stack size.
"""
import argparse,json

def best(slots,chance,specialty=False,queens=6,cycle=550,stack=1):
 rows=[]
 for speed in range(slots+1):
  for parallel in range(slots-speed+1):
   for productivity in range(slots-speed-parallel+1):
    for rare in range(slots-speed-parallel-productivity+1) if specialty else [0]:
     focus=slots-speed-parallel-productivity-rare
     if not specialty and focus:continue
     multiplier=(1+.25*productivity)*((1+.5*rare)*(1+focus) if specialty else 1)
     ticks=max(20,cycle//(1+speed));expected=queens*1200/ticks*(1+parallel)*min(1,chance*multiplier)*stack
     rows.append(dict(speed=speed,parallel=parallel,productivity=productivity,specialty=rare,focus=focus,cycle_ticks=ticks,items_per_minute=expected))
 return sorted(rows,key=lambda r:(-r['items_per_minute'],r['productivity'],r['specialty']))
if __name__=='__main__':
 a=argparse.ArgumentParser(description=__doc__);a.add_argument('--slots',type=int,default=7);a.add_argument('--chance',type=float,default=.15);a.add_argument('--specialty',action='store_true');a.add_argument('--queens',type=int,default=6);a.add_argument('--cycle',type=int,default=550);a.add_argument('--stack',type=int,default=1);v=a.parse_args()
 if not 0<=v.slots<=8 or v.chance<0 or v.cycle<20:a.error('invalid slots, chance or cycle')
 print(json.dumps(best(v.slots,v.chance,v.specialty,v.queens,v.cycle,v.stack)[:5],indent=2))
