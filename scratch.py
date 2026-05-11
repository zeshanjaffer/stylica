import os
import re

def migrate_to_sdp_ssp(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".xml"):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()

                # Function to process dp matches
                def replace_dp(match):
                    val = int(match.group(1))
                    if val == 0:
                        return '"0dp"'
                    elif val > 600:
                        return f'"{val}dp"'
                    return f'"@dimen/_{val}sdp"'

                # Function to process sp matches
                def replace_sp(match):
                    val = int(match.group(1))
                    if val == 0:
                        return '"0sp"'
                    elif val > 600:
                        return f'"{val}sp"'
                    return f'"@dimen/_{val}ssp"'
                
                content = re.sub(r'"(\d+)dp"', replace_dp, content)
                content = re.sub(r'"(\d+)sp"', replace_sp, content)
                
                with open(filepath, 'w') as f:
                    f.write(content)

if __name__ == "__main__":
    migrate_to_sdp_ssp("app/src/main/res/layout")
