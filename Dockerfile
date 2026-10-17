# হালকা ওজনের অ্যালপাইন লিনাক্স ইমেজ ব্যবহার করা হলো
FROM alpine:latest

# সার্টিফিকেট ইনস্টল করা (HTTPS এর জন্য দরকার হতে পারে)
RUN apk add --no-cache ca-certificates

# ওয়ার্কিং ডিরেক্টরি সেট করা
WORKDIR /app

# ফাইলগুলো কন্টেইনারে কপি করা
COPY fsb /app/fsb
COPY fsb.env /app/fsb.env

# fsb ফাইলটিকে রান করার পারমিশন দেওয়া
RUN chmod +x /app/fsb

# আপনার পোর্টটি এক্সপোজ করা
EXPOSE 43969

# বট রান করার কমান্ড
CMD ["./fsb", "run"]