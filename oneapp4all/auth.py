def checkauth(code):
    with open("auth.txt","r") as f: cc = f.readlines()
    cc = cc[0].strip()
    if code==cc: return True
    else: return False

