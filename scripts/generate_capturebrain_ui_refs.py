#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import textwrap

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "ui_reference" / "images"
OUT.mkdir(parents=True, exist_ok=True)

W, H = 1080, 2400
BG = "#07111F"
PANEL = "#0D1828"
CARD = "#101D30"
CARD2 = "#13243A"
BORDER = "#22344F"
TEXT = "#F5F7FB"
MUTED = "#9BA9BE"
SUBTLE = "#667489"
BLUE = "#4C7DFF"
MINT = "#31E6C3"
GREEN = "#31D07F"
YELLOW = "#FFCC66"
RED = "#FF6B7C"

FONT_REG = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
FONT_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
FONT_MONO = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"

def font(size, bold=False, mono=False):
    return ImageFont.truetype(FONT_MONO if mono else (FONT_BOLD if bold else FONT_REG), size)

def rr(draw, box, r=32, fill=CARD, outline=BORDER, width=2):
    draw.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=width)

def pill(draw, xy, label, fill, fg=TEXT, pad_x=22, pad_y=10, size=28, bold=True):
    x, y = xy
    f = font(size, bold=bold)
    b = draw.textbbox((0,0), label, font=f)
    w = b[2]-b[0]+pad_x*2
    h = b[3]-b[1]+pad_y*2
    draw.rounded_rectangle((x,y,x+w,y+h), radius=h//2, fill=fill)
    draw.text((x+pad_x, y+pad_y-3), label, font=f, fill=fg)
    return x+w, y+h

def text(draw, xy, s, size=36, fill=TEXT, bold=False, mono=False, anchor=None, max_width=None, line_gap=8):
    f = font(size, bold=bold, mono=mono)
    x, y = xy
    if max_width is None:
        draw.text((x,y), s, font=f, fill=fill, anchor=anchor)
        return y + draw.textbbox((x,y), s, font=f)[3] - y
    lines=[]
    for para in str(s).split("\n"):
        cur=""
        for ch in para:
            test = cur + ch
            if draw.textlength(test, font=f) <= max_width or not cur:
                cur = test
            else:
                lines.append(cur)
                cur = ch
        if cur:
            lines.append(cur)
        else:
            lines.append("")
    yy=y
    for line in lines:
        draw.text((x,yy), line, font=f, fill=fill)
        yy += size + line_gap
    return yy

def gradient_bg():
    img = Image.new('RGB', (W,H), BG)
    pix = img.load()
    for y in range(H):
        for x in range(W):
            # cheap radial-ish glow from top right and lower left
            base = (7,17,31)
            d1 = max(0, 1 - ((x-850)**2 + (y-180)**2) ** 0.5 / 950)
            d2 = max(0, 1 - ((x-120)**2 + (y-1850)**2) ** 0.5 / 900)
            r = int(base[0] + d1*25 + d2*4)
            g = int(base[1] + d1*35 + d2*18)
            b = int(base[2] + d1*85 + d2*58)
            pix[x,y]=(r,g,b)
    return img.filter(ImageFilter.GaussianBlur(radius=0.3))

def status_bar(d):
    text(d, (72, 48), "12:55", 42, TEXT, True)
    text(d, (820, 48), "5G  ◌  100", 36, TEXT, True)

def nav_bar(d, active="홈"):
    rr(d, (54, 2150, W-54, 2310), r=46, fill="#0A1424", outline="#18283F", width=2)
    items=[("시작","★"),("홈","⌂"),("상세","▣"),("설정","⚙")]
    xs=[170,405,675,920]
    for label, icon in items:
        i=items.index((label,icon)); x=xs[i]
        if label==active:
            d.rounded_rectangle((x-86,2178,x+86,2262), radius=42, fill="#182B4B")
            text(d,(x,2190),icon,38,MINT,True,anchor="ma")
            text(d,(x,2248),label,26,TEXT,True,anchor="ma")
        else:
            text(d,(x,2196),icon,36,SUBTLE,True,anchor="ma")
            text(d,(x,2250),label,26,SUBTLE,False,anchor="ma")

def header(d, subtitle=True):
    text(d, (70, 130), "CaptureBrain", 56, TEXT, True)
    if subtitle:
        text(d, (70, 202), "스크린샷을 Markdown 세컨드브레인으로", 28, MUTED)

def screen_onboarding():
    img=gradient_bg(); d=ImageDraw.Draw(img); status_bar(d); header(d)
    # hero
    rr(d,(56,300,1024,910),r=48,fill="#0E1A2E",outline="#263C60")
    pill(d,(96,342),"PRIVATE · USER DRIVE", "#143B4C", MINT, size=24)
    text(d,(96,430),"스크린샷이\n지식 노트로",78,TEXT,True,line_gap=0)
    text(d,(96,635),"찍기만 하면 OCR → Markdown → Google Drive 저장까지 자동으로 정리합니다.",34,MUTED,max_width=780,line_gap=14)
    # faux orbit
    d.ellipse((770,380,920,530), outline="#2CD7BE", width=5)
    d.rounded_rectangle((738,565,950,705), radius=28, fill="#132848", outline="#365889", width=2)
    text(d,(763,593),"OCR",34,MINT,True)
    text(d,(763,645),"Markdown",26,MUTED)
    # steps
    y=960
    steps=[("1","이미지 권한","READ_MEDIA_IMAGES만 요청합니다",MINT),("2","자동 감지","새 스크린샷을 큐에 등록",BLUE),("3","Google Drive 연결","내 계정 Drive에만 저장",GREEN)]
    for no,title,body,c in steps:
        rr(d,(56,y,1024,y+190),r=34,fill=CARD,outline=BORDER)
        d.ellipse((96,y+48,180,y+132),fill="#152A45",outline=c,width=3)
        text(d,(138,y+65),no,34,c,True,anchor="ma")
        text(d,(212,y+48),title,38,TEXT,True)
        text(d,(212,y+102),body,28,MUTED)
        y+=220
    rr(d,(56,1655,1024,1835),r=34,fill="#0D2430",outline="#245B66")
    text(d,(96,1702),"방식",28,MINT,True)
    text(d,(96,1750),"서버 업로드 없음 · 내 Google Drive 저장",34,TEXT,True)
    d.rounded_rectangle((90,1905,990,2048),radius=42,fill=BLUE)
    text(d,(540,1944),"자동 처리 시작",42,"#FFFFFF",True,anchor="ma")
    nav_bar(d,"시작")
    return img

def metric(d, box, num, label, color):
    rr(d,box,r=32,fill=CARD,outline=BORDER)
    x1,y1,x2,y2=box
    text(d,(x1+42,y1+36),str(num),70,TEXT,True)
    text(d,(x1+42,y1+122),label,30,MUTED)
    d.ellipse((x2-76,y1+48,x2-42,y1+82),fill=color)

def screen_home():
    img=gradient_bg(); d=ImageDraw.Draw(img); status_bar(d); header(d)
    rr(d,(56,300,1024,575),r=42,fill="#0E1F35",outline="#294E75")
    pill(d,(96,342),"자동 감지 ON", "#123E3B", MINT, size=28)
    text(d,(96,420),"새 스크린샷을\nMarkdown으로 변환 중",44,TEXT,True,line_gap=4)
    d.rounded_rectangle((820,365,960,445),radius=40,fill="#143A63")
    d.ellipse((890,374,950,434),fill=MINT)
    text(d,(96,520),"연결 계정: syas0301@gmail.com",26,MUTED)
    metric(d,(56,620,526,850),4,"오늘 완료",GREEN)
    metric(d,(554,620,1024,850),0,"대기",BLUE)
    metric(d,(56,878,526,1108),0,"실패",RED)
    metric(d,(554,878,1024,1108),0,"확인 필요",YELLOW)
    d.rounded_rectangle((56,1165,526,1295),radius=36,fill=BLUE)
    text(d,(291,1202),"최근 스크린샷 스캔",32,"#fff",True,anchor="ma")
    rr(d,(554,1165,1024,1295),r=36,fill="#12233A",outline="#2A4061")
    text(d,(789,1202),"실패 재시도",32,TEXT,True,anchor="ma")
    text(d,(56,1370),"최근 캡처",46,TEXT,True)
    rows=[("전남대 도서관 소식지","Chrome · 방금 전","완료"),("모바일 UI 레퍼런스","Gallery · 12:58","완료"),("OAuth 설정 가이드","Chrome · 00:27","완료")]
    y=1448
    for title,meta,st in rows:
        rr(d,(56,y,1024,y+190),r=32,fill=CARD,outline=BORDER)
        d.rounded_rectangle((92,y+38,176,y+122),radius=22,fill="#152A45")
        text(d,(134,y+55),"MD",28,MINT,True,anchor="ma")
        text(d,(210,y+36),title,34,TEXT,True,max_width=520)
        text(d,(210,y+92),meta,26,MUTED)
        pill(d,(820,y+58),st,"#123E3B",MINT,size=25)
        text(d,(210,y+135),"Drive / CaptureBrain / Research",22,SUBTLE)
        y+=215
    nav_bar(d,"홈")
    return img

def screen_detail():
    img=gradient_bg(); d=ImageDraw.Draw(img); status_bar(d); header(d, False)
    text(d,(70,218),"캡처 상세",48,TEXT,True)
    rr(d,(56,300,1024,780),r=40,fill=CARD,outline=BORDER)
    d.rounded_rectangle((96,350,984,600),radius=26,fill="#E8EDF5")
    text(d,(130,380),"12:33 DA",34,"#111827",True)
    text(d,(130,440),"전남대 도서관 소식지",48,"#111827",True)
    text(d,(130,520),"cnulib.tistory.com · 공지사항",30,"#334155")
    text(d,(96,640),"전남대 도서관 소식지",40,TEXT,True)
    pill(d,(96,700),"Drive 업로드 완료","#123E3B",MINT,size=24)
    pill(d,(370,700),"OCR 96%","#13294F",BLUE,size=24)
    pill(d,(550,700),"Chrome","#18233A",MUTED,size=24)
    rr(d,(56,825,1024,1185),r=34,fill=CARD,outline=BORDER)
    text(d,(96,870),"추출 텍스트",36,TEXT,True)
    body="전남대 도서관 소식지\n도서관 이용 안내와 신규 전자자료 소식을 확인하세요. 학술정보 검색, 열람실 운영시간, 교육 일정이 포함되어 있습니다."
    text(d,(96,930),body,29,MUTED,max_width=860,line_gap=10)
    rr(d,(56,1228,1024,1715),r=34,fill="#0B1726",outline="#27405F")
    text(d,(96,1272),"Markdown 미리보기",36,TEXT,True)
    md="# 전남대 도서관 소식지\n\n- source: Chrome\n- folder: Research/Chrome\n\n## 원문 요약\n도서관 공지와 전자자료 안내..."
    text(d,(96,1338),md,27,"#D7E0EF",mono=False,max_width=845,line_gap=12)
    rr(d,(56,1760,1024,1940),r=34,fill="#0D2430",outline="#245B66")
    text(d,(96,1802),"Drive 경로",28,MINT,True)
    text(d,(96,1850),"CaptureBrain / Research / Chrome",32,TEXT,True)
    d.rounded_rectangle((56,1982,510,2110),radius=36,fill=BLUE)
    text(d,(283,2018),"Drive에서 열기",32,"#fff",True,anchor="ma")
    rr(d,(535,1982,1024,2110),r=36,fill="#12233A",outline="#2A4061")
    text(d,(780,2018),"다시 처리",32,TEXT,True,anchor="ma")
    nav_bar(d,"상세")
    return img

def settings_row(d,y,label,desc,on=True):
    text(d,(96,y),label,32,TEXT,True)
    text(d,(96,y+48),desc,24,MUTED)
    d.rounded_rectangle((840,y+10,960,y+74),radius=32,fill="#143A63" if on else "#233047")
    d.ellipse((902 if on else 850,y+17,952 if on else 900,y+67),fill=MINT if on else SUBTLE)

def screen_settings():
    img=gradient_bg(); d=ImageDraw.Draw(img); status_bar(d); header(d,False)
    text(d,(70,218),"설정",48,TEXT,True)
    rr(d,(56,300,1024,660),r=40,fill=CARD,outline=BORDER)
    text(d,(96,350),"Google Drive 연결",38,TEXT,True)
    text(d,(96,410),"syas0301@gmail.com",32,MUTED)
    pill(d,(96,472),"drive.file 최소 권한","#13294F",BLUE,size=24)
    pill(d,(390,472),"내 Drive 저장","#123E3B",MINT,size=24)
    rr(d,(96,560,984,625),r=24,fill="#12233A",outline="#2A4061")
    text(d,(540,575),"연결 해제",28,TEXT,True,anchor="ma")
    rr(d,(56,715,1024,1288),r=40,fill=CARD,outline=BORDER)
    settings_row(d,760,"Wi‑Fi에서만 업로드","모바일 데이터 사용을 줄입니다",True)
    settings_row(d,900,"민감정보 확인 후 업로드","캡처를 Drive에 올리기 전 확인",True)
    settings_row(d,1040,"온디바이스 OCR 우선","가능하면 기기 안에서 먼저 처리",True)
    settings_row(d,1180,"자동 폴더 분류","앱/주제별 Drive 폴더 생성",True)
    rr(d,(56,1340,1024,1555),r=36,fill="#251A27",outline="#584160")
    text(d,(96,1388),"민감한 캡처는 업로드 전 확인",34,TEXT,True)
    text(d,(96,1445),"주민번호, 결제정보, 메신저 대화가 감지되면 먼저 확인 화면을 보여줍니다.",27,MUTED,max_width=850,line_gap=9)
    rr(d,(56,1605,1024,1855),r=36,fill=CARD,outline=BORDER)
    text(d,(96,1650),"Markdown 템플릿",34,TEXT,True)
    text(d,(96,1710),"기본 노트 · 원문 보존형",30,MUTED)
    text(d,(96,1780),"루트 폴더",28,MINT,True)
    text(d,(300,1780),"CaptureBrain",30,TEXT,True)
    text(d,(82,1915),"CaptureBrain 서버에는 스크린샷을 업로드하지 않습니다.",27,MUTED,max_width=880)
    nav_bar(d,"설정")
    return img

screens=[
    ("01_onboarding", screen_onboarding()),
    ("02_home_dashboard", screen_home()),
    ("03_capture_detail", screen_detail()),
    ("04_settings_privacy", screen_settings()),
]
for name,img in screens:
    img.save(OUT / f"{name}.png")

# Board image
scale=0.38
thumbs=[]
for _,img in screens:
    thumbs.append(img.resize((int(W*scale), int(H*scale))))
BW = thumbs[0].width*4 + 120*5
BH = thumbs[0].height + 360
board = Image.new('RGB',(BW,BH),BG)
bd=ImageDraw.Draw(board)
text(bd,(80,55),"CaptureBrain UI Reference Board",60,TEXT,True)
text(bd,(82,132),"Premium Android · Korean-first · User-owned Google Drive · Privacy-first",32,MUTED)
x=120
labels=["Onboarding","Home dashboard","Capture detail","Settings & privacy"]
for i,t in enumerate(thumbs):
    board.paste(t,(x,220))
    bd.rounded_rectangle((x-3,217,x+t.width+3,223+t.height),radius=30,outline="#2B4770",width=4)
    text(bd,(x,220+t.height+28),labels[i],34,TEXT,True)
    x += t.width + 120
board.save(OUT / "capturebrain_ui_reference_board.png")
print(OUT)
for p in sorted(OUT.glob('*.png')):
    print(p)
