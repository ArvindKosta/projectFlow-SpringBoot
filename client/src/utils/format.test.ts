import { describe, expect, it } from "vitest";
import { cn } from "./format";

describe("cn", () => {
  it("joins truthy class names", () => {
    expect(cn("a", false, "b", undefined)).toBe("a b");
  });
});
