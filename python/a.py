from bs4 import BeautifulSoup

html = open('gcolor.html')

soup = BeautifulSoup(html, 'html.parser')

f = open('out.txt', 'w')

names = soup.find_all('span', attrs={"name"})
shades = soup.find_all('span', attrs={"shade"})
hexes = soup.find_all('span', attrs={"hex"})

n = 0
s = 1
h = 1
i = 14

while h < len(hexes) - 1:
	hx = hexes[h].string.upper().replace("#", "0x")
	sd = shades[s].string;
	nm = names[n].string.replace(" ", "_").upper()

	if sd == "50":
		f.write ("public static Color " + nm + "_" + sd + "   = new Color(" + hx + "FF);\n")
	elif sd[0] == "A":
		f.write ("public static Color " + nm + "_" + sd + " = new Color(" + hx + "FF);\n")
	else:
		f.write ("public static Color " + nm + "_" + sd + "  = new Color(" + hx + "FF);\n")

	if nm == "BROWN":
		i = 10
	elif nm == "GREY":
		i = 10
	elif nm == "BLUE_GREY":
		i = 10
	else:
		i = 14

	if(s < i):
		s += 1 # increment while less than the shaes
	else:
		s = 1 # reset shades (set to one to skip first shade [repeat of 500])
		if(n < len(names)-1):
			n += 1 # add one to names to get next name
		h += 1 # add one to hexes to skip first hex of each name block (first hex is repeat of 500)
		f.write("\n")


	h += 1 # increment hex


f.close()