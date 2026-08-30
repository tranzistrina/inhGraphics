#!/usr/bin/env python3
"""Мини RCON-клиент: rcon.py "команда" [ещё команды...]"""
import socket, struct, sys

def send(sock, req_id, ptype, body):
    data = struct.pack("<ii", req_id, ptype) + body.encode("utf-8") + b"\x00\x00"
    sock.send(struct.pack("<i", len(data)) + data)

def recv_exact(sock, n):
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("closed")
        buf += chunk
    return buf

def read_packet(sock):
    (length,) = struct.unpack("<i", recv_exact(sock, 4))
    data = recv_exact(sock, length)
    rid, ptype = struct.unpack("<ii", data[:8])
    body = data[8:-2].decode("utf-8", errors="replace")
    return rid, ptype, body

def main():
    sock = socket.create_connection(("127.0.0.1", 25575), timeout=15)
    send(sock, 1, 3, "test123")
    rid, _, _ = read_packet(sock)
    if rid == -1:
        print("AUTH FAILED"); sys.exit(1)
    for cmd in sys.argv[1:]:
        send(sock, 2, 2, cmd)
        rid, _, body = read_packet(sock)
        out = body.replace("\u00a7", "") if "\u00a7" in repr(body) else body
        print(f">>> {cmd}\n{out}")
    sock.close()

main()
