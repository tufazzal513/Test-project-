FROM alpine:latest

# গিট এবং সার্টিফিকেট ইনস্টল করা (ক্লোন করার জন্য দরকার)
RUN apk add --no-cache git ca-certificates

# ওয়ার্কিং ডিরেক্টরি সেট করা
WORKDIR /app

# গিটহাব থেকে আপনার রিপো ক্লোন করা
# নিচের লিংকটি আপনার নিজের রিপোর লিংক দিয়ে পরিবর্তন করুন
RUN git clone https://github.com/tufazzal513/Test-project-.git .

# fsb ফাইলটিকে রান করার পারমিশন দেওয়া
RUN chmod +x ./fsb

# পোর্ট এক্সপোজ করা (আপনার দেওয়া পোর্ট অনুযায়ী)
EXPOSE 43969

# বট রান করা
CMD ["./fsb", "run"]
